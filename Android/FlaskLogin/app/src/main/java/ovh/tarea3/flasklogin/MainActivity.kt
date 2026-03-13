package ovh.tarea3.flasklogin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ovh.tarea3.flasklogin.ui.screens.LoginScreen
import ovh.tarea3.flasklogin.ui.screens.RegisterScreen
import ovh.tarea3.flasklogin.ui.screens.WelcomeScreen
import ovh.tarea3.flasklogin.ui.screens.UsersScreen
import ovh.tarea3.flasklogin.ui.theme.FlaskLoginTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlaskLoginTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { username -> 
                    navController.navigate("welcome/$username") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("login") },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(
            route = "welcome/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
            WelcomeScreen(
                username = username,
                onLogout = { navController.navigate("login") {
                    popUpTo("welcome/{username}") { inclusive = true }
                }},
                onNavigateToUsers = { navController.navigate("users") }
            )
        }
        composable("users") {
            UsersScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
