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
import com.example.jetpack_compose_foot_calendar.navigation.Routes.matchDetail
import com.example.jetpack_compose_foot_calendar.ui.auth.AuthViewModel
import com.example.jetpack_compose_foot_calendar.ui.auth.LoginScreen
import com.example.jetpack_compose_foot_calendar.ui.calendar.CalendarScreen
import com.example.jetpack_compose_foot_calendar.ui.calendar.CalendarViewModel
import com.example.jetpack_compose_foot_calendar.ui.matchdetail.MatchDetailScreen
import com.example.jetpack_compose_foot_calendar.ui.matchdetail.MatchDetailViewModel
import com.example.jetpack_compose_foot_calendar.ui.profile.ProfileScreen
import com.example.jetpack_compose_foot_calendar.ui.profile.ProfileViewModel

/**
 * Centralised route definitions for the application.
 *
 * All navigation route strings are declared here to avoid magic strings scattered across the
 * codebase. Use the helper [matchDetail] to build parametrised routes safely.
 */
object Routes {
    /** Route for the login screen. */
    const val LOGIN = "login"

    /** Route for the main calendar screen. */
    const val CALENDAR = "calendar"

    /**
     * Route template for the match detail screen.
     * The `{fixtureId}` segment is an [Int] argument.
     */
    const val MATCH_DETAIL = "match_detail/{fixtureId}"

    /** Route for the user profile screen. */
    const val PROFILE = "profile"

    /**
     * Builds the concrete navigation route to the match detail screen.
     *
     * @param fixtureId The unique identifier of the fixture to display.
     * @return A fully resolved route string, e.g. `"match_detail/123"`.
     */
    fun matchDetail(fixtureId: Int) = "match_detail/$fixtureId"
}

/**
 * Root navigation graph for the application.
 *
 * Instantiates and shares ViewModels across the full navigation scope so that state is
 * preserved when navigating back and forth between screens. An authentication guard
 * implemented via [LaunchedEffect] redirects unauthenticated users to [Routes.LOGIN].
 *
 * @param navController The [NavHostController] that drives navigation actions.
 * @param repository    The [FootballRepository] injected into ViewModels that require data access.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: FootballRepository
) {
    // ViewModels shared across the entire navigation scope
    val authViewModel: AuthViewModel = viewModel()
    val calendarViewModel: CalendarViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CalendarViewModel(repository) as T
            }
        }
    )
    val profileViewModel: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(repository) as T
            }
        }
    )

    // Authentication guard — redirect to login whenever the user is not authenticated
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
            val fixtureId = backStackEntry.arguments?.getInt("fixtureId") ?: return@composable
            val matchDetailViewModel: MatchDetailViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return MatchDetailViewModel(repository) as T
                    }
                }
            )
            MatchDetailScreen(
                fixtureId = fixtureId,
                viewModel = matchDetailViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                viewModel = profileViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}