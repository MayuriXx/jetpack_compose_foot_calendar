package com.example.jetpack_compose_foot_calendar

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import com.example.jetpack_compose_foot_calendar.data.api.RetrofitClient
import com.example.jetpack_compose_foot_calendar.data.cache.CacheManager
import com.example.jetpack_compose_foot_calendar.data.repository.FootballRepository
import com.example.jetpack_compose_foot_calendar.navigation.AppNavGraph
import com.example.jetpack_compose_foot_calendar.ui.theme.JetPackComposeFootCalendarTheme

/**
 * Top-level DataStore delegate.
 *
 * Creates a single [DataStore] instance scoped to the application context under the key
 * `"football_cache"`. Using a top-level property with [preferencesDataStore] guarantees that
 * only one instance is ever created, regardless of how many times [MainActivity] is recreated.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "football_cache")

/**
 * The single [ComponentActivity] of the application.
 *
 * Responsible for:
 * - Creating the [CacheManager] backed by the app-scoped [DataStore].
 * - Instantiating [FootballRepository] with the Retrofit API client and the cache.
 * - Setting up the Compose content tree rooted at [AppNavGraph].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cacheManager = CacheManager(applicationContext.dataStore)
        val repository = FootballRepository(
            api = RetrofitClient.footballApi,
            cache = cacheManager
        )

        setContent {
            JetPackComposeFootCalendarTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    repository = repository
                )
            }
        }
    }
}