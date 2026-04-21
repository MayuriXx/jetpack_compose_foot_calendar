package com.example.jetpack_compose_foot_calendar

import AppNavGraph
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
import com.example.jetpack_compose_foot_calendar.ui.theme.JetPackComposeFootCalendarTheme

// Delegate de haut niveau — crée le DataStore une seule fois pour l'application
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "football_cache")

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