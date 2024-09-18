package com.attendance.attendancemate

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.attendance.attendancemate.ui.screen.AttendanceScreen
import com.attendance.attendancemate.ui.screen.LoginScreen
import com.attendance.attendancemate.ui.theme.AttendanceMateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SharedPreferences for version management
        val sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val currentVersionCode = 5
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
        composable("attendance") {
            AttendanceScreen(navController)
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