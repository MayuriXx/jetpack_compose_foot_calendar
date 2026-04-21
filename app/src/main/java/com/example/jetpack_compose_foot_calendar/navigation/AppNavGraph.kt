package com.example.jetpack_compose_foot_calendar.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.jetpack_compose_foot_calendar.data.repository.FootballRepository
import com.example.jetpack_compose_foot_calendar.ui.auth.AuthViewModel
import com.example.jetpack_compose_foot_calendar.ui.auth.LoginScreen
import com.example.jetpack_compose_foot_calendar.ui.calendar.CalendarScreen
import com.example.jetpack_compose_foot_calendar.ui.calendar.CalendarViewModel
import com.example.jetpack_compose_foot_calendar.ui.matchdetail.MatchDetailViewModel
import com.example.jetpack_compose_foot_calendar.ui.profile.ProfileViewModel

object Routes {
    const val LOGIN = "login"
    const val CALENDAR = "calendar"
    const val MATCH_DETAIL = "match_detail/{fixtureId}"
    const val PROFILE = "profile"

    fun matchDetail(fixtureId: Int) = "match_detail/$fixtureId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: FootballRepository
) {
    // ViewModels partagés sur toute la navigation
    val authViewModel: AuthViewModel = viewModel()
    val calendarViewModel: CalendarViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CalendarViewModel(repository) as T
            }
        }
    )
    val profileViewModel: ProfileViewModel = viewModel()

    // Guard d'authentification
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.CALENDAR) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CALENDAR) {
            CalendarScreen(
                viewModel = calendarViewModel,
                onMatchClick = { fixtureId ->
                    navController.navigate(Routes.matchDetail(fixtureId))
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                }
            )
        }

        composable(
            route = Routes.MATCH_DETAIL,
            arguments = listOf(navArgument("fixtureId") { type = NavType.IntType })
        ) { backStackEntry ->
            val fixtureId = backStackEntry.arguments?.getInt("fixtureId")
            val matchDetailViewModel: MatchDetailViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MatchDetailViewModel(repository) as T
                    }
                }
            )
            // MatchDetailScreen — étape suivante
        }

        composable(Routes.PROFILE) {
            // ProfileScreen — étape suivante
        }
    }
}