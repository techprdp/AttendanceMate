package com.attendance.attendancemate.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
@Composable
fun BottomNavScreen(
    navController: NavHostController,
    sessionData: String?,
    email:String?,
    date: String?
) {
    val bottomNavController = rememberNavController()

    // Immediately navigate to attendance if sessionData is not null
    LaunchedEffect(sessionData) {
        if (sessionData != null) {
            bottomNavController.navigate("attendance?sessionData=$sessionData&email=$email&date=$date") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Attendance") },
                    selected = false,
                    onClick = {
                        bottomNavController.navigate("attendance?sessionData=$sessionData&email=$email&date=$date")
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        bottomNavController.navigate("settings")
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "placeholder", // Static placeholder
            Modifier.padding(innerPadding)
        ) {
            // Placeholder route, will navigate away immediately
            composable("placeholder") {}

            composable(
                "attendance?sessionData={sessionData}&email={email}&date={date}",
                arguments = listOf(
                    navArgument("sessionData") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType },
                    navArgument("date") { type = NavType.StringType },
                )
            ) { backStackEntry ->
                val sessionDataJson = backStackEntry.arguments?.getString("sessionData")
                val userEmail = backStackEntry.arguments?.getString("email")
                val initialDate = backStackEntry.arguments?.getString("date")
                AttendanceScreen(navController, paramSessionData = sessionDataJson, userEmail, initialDate)
            }

            composable("settings") {
                SettingScreen(
                    navController
                )
            }
        }
    }
}