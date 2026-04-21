import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.jetpack_compose_foot_calendar.data.repository.FootballRepository

object Routes {
    const val LOGIN = "login"
    const val CALENDAR = "calendar"
    const val MATCH_DETAIL = "match_detail/{fixtureId}"
    const val PROFILE = "profile"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: FootballRepository
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
        }

        composable(Routes.CALENDAR) {
        }

        composable(
            route = Routes.MATCH_DETAIL,
            arguments = listOf(
                navArgument("fixtureId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val fixtureId = backStackEntry.arguments?.getInt("fixtureId")
        }

        composable(Routes.PROFILE) {
        }
    }
}