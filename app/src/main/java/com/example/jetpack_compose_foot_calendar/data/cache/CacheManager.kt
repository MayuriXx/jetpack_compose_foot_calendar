package com.example.jetpack_compose_foot_calendar.data.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.first

/**
 * TTL-based key-value cache backed by [DataStore]<[Preferences]> with Gson serialisation.
 *
 * Each entry is stored as a JSON-serialised [CacheEntry] that includes the payload, the
 * write timestamp, and a TTL in minutes. On read, a stale entry (age > TTL) is discarded
 * and `null` is returned so that the caller can fetch fresh data.
 *
 * All operations are `suspend` functions and safe to call from any coroutine context.
 *
 * @param dataStore The [DataStore] instance used for persistent storage.
 */
class CacheManager(private val dataStore: DataStore<Preferences>) {

    /**
     * Stores [value] under [key] with a time-to-live of [ttlMinutes] minutes.
     *
     * The value is serialised to JSON using Gson and wrapped in a [CacheEntry] that records the
     * current system time. Any previously stored value for the same key is overwritten.
     *
     * @param key        Cache key used to retrieve the value later.
     * @param value      The object to persist. Must be serialisable by Gson.
     * @param ttlMinutes How many minutes the entry is considered valid.
     */
    suspend fun <T> set(key: String, value: T, ttlMinutes: Int) {
        val entry = CacheEntry(
            data = Gson().toJson(value),
            timestamp = System.currentTimeMillis(),
            ttlMinutes = ttlMinutes
        )
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(key)] = Gson().toJson(entry)
        }
    }

    /**
     * Retrieves the cached value for [key], or returns `null` if the entry is absent or stale.
     *
     * This inline function uses a reified type parameter to delegate the actual deserialisation
     * to [getInternal], which cannot itself be `inline`.
     *
     * @param T   The expected type of the cached value.
     * @param key The cache key.
     * @return    The deserialised value, or `null` if not found / expired / deserialisation error.
     */
    suspend inline fun <reified T> get(key: String): T? = getInternal(key, T::class.java)

    /**
     * Internal implementation of the cache retrieval logic.
     *
     * Reads the raw JSON from [DataStore], deserialises it into a [CacheEntry], checks the TTL,
     * and then deserialises the payload into type [T]. Any exception during this process
     * (corrupted data, class mismatch, etc.) is silently swallowed and `null` is returned.
     *
     * @param key   The cache key.
     * @param clazz The [Class] of the expected return type.
     * @return      The deserialised value, or `null` on any failure or expiry.
     */
    @PublishedApi
    internal suspend fun <T> getInternal(key: String, clazz: Class<T>): T? {
        return try {
            val prefs = dataStore.data.first()
            val json = prefs[stringPreferencesKey(key)] ?: return null
            val entry = Gson().fromJson(json, CacheEntry::class.java)

            // Vérifie si le TTL est dépassé
            val ageMinutes = (System.currentTimeMillis() - entry.timestamp) / 60000
            if (ageMinutes > entry.ttlMinutes) return null

            Gson().fromJson(entry.data, clazz)
        } catch (_: Exception) {
            null  // En cas d'erreur de désérialisation, on ignore le cache
        }
    }

    /**
     * Removes all entries from the cache.
     *
     * Use this when the user logs out or when a full data refresh is required.
     */
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}

/**
 * Internal envelope stored in [DataStore] for each cached item.
 *
 * @property data       The Gson-serialised payload as a JSON string.
 * @property timestamp  Unix epoch milliseconds at the time of writing.
 * @property ttlMinutes Maximum number of minutes the entry should be considered valid.
 */
internal data class CacheEntry(
    val data: String,
    val timestamp: Long,
    val ttlMinutes: Int
)