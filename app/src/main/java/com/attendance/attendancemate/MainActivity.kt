package com.attendance.attendancemate

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.attendance.attendancemate.ui.screen.BottomNavScreen
import com.attendance.attendancemate.ui.screen.LoginScreen
import com.attendance.attendancemate.ui.theme.AttendanceMateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SharedPreferences for version management
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val currentVersionCode = 8
        val storedVersionCode = sharedPreferences.getInt("version_code", 1)

        // Define the version codes for which cleanup is required
        val versionsToClean = setOf(1, 2, 3, 4)
        if (storedVersionCode in versionsToClean) {
            // App is at a version that needs cleanup
            performCleanup(sharedPreferences)
        }

        // Update the stored version code
        sharedPreferences.edit()
            .putInt("version_code", currentVersionCode)
            .apply()

        enableEdgeToEdge()
        setContent {
            AttendanceMateTheme {
                val navController = rememberNavController()
                MainNavHost(navController)
            }
        }
    }
}

@Composable
fun MainNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }

        composable(
            "bottomNav?sessionData={sessionData}&email={email}&date={date}",
            arguments = listOf(
                navArgument("sessionData") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val sessionDataJson = backStackEntry.arguments?.getString("sessionData")
            val email = backStackEntry.arguments?.getString("email")
            val date = backStackEntry.arguments?.getString("date")
            BottomNavScreen(navController, sessionDataJson, email, date)
        }
    }
}

private fun performCleanup(sharedPreferences: SharedPreferences) {
    val editor = sharedPreferences.edit()

    // Remove old parameters
    editor.remove("cookie")
    editor.remove("email")
    editor.remove("password")
    editor.remove("date")

    editor.apply()
}