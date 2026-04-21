package com.example.jetpack_compose_foot_calendar.data.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.first


class CacheManager(private val dataStore: DataStore<Preferences>) {

    // Sauvegarde une valeur avec un TTL (durée de vie)
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

    // Délègue à une fonction privée pour contourner la restriction inline/internal
    suspend inline fun <reified T> get(key: String): T? = getInternal(key, T::class.java)

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

    // Vide tout le cache — équivalent de ton cacheService.clear()
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}

// Structure interne du cache
internal data class CacheEntry(
    val data: String,
    val timestamp: Long,
    val ttlMinutes: Int
)