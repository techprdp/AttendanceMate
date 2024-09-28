package com.attendance.attendancemate.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.attendance.attendancemate.view.SharedViewModel

@Composable
fun SettingScreen(
    navController: NavHostController,
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val isSavingData by sharedViewModel.dataRetentionAgreed.observeAsState(false)
    var showPolicyDialog by remember { mutableStateOf(false) }
    var showDeletionNoticeDialogOnAutoLogin by remember { mutableStateOf(false) }
    var showDeletionNoticeDialogOnLogOut by remember { mutableStateOf(false) }

    // Retrieve app version name
    val context = LocalContext.current
    val versionName = remember {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Main content of the settings screen
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Settings", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Auto-login toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto Login", modifier = Modifier.weight(1f))
                Switch(
                    checked = isSavingData,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            // Show the policy dialog if the toggle is being turned on
                            showPolicyDialog = true
                        } else {
                            // Show the data deletion notice dialog if the toggle is turned off
                            showDeletionNoticeDialogOnAutoLogin = true
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout button
            Button(
                onClick = {
                    if (isSavingData)
                        showDeletionNoticeDialogOnLogOut = true
                    else
                        onLogoutClicked(navController, sharedViewModel)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }

        // Version name displayed at the bottom
        Text(
            text = "v$versionName",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        // Show the Data Retention Policy Dialog when required
        if (showPolicyDialog) {
            DataRetentionPolicyDialog(
                onAgree = {
                    sharedViewModel.setDataRetentionAgreed(true)
                    // Log the user out after agreeing
                    onLogoutClicked(navController, sharedViewModel)
                    showPolicyDialog = false // Dismiss the dialog after agreement
                },
                onDisagree = {
                    // Dismiss the dialog but keep the toggle as it was (not saving data)
                    sharedViewModel.setDataRetentionAgreed(false)
                    showPolicyDialog = false
                }
            )
        }

        // Show the Data Deletion Notice Dialog when the toggle is turned off
        if (showDeletionNoticeDialogOnAutoLogin) {
            DataDeletionNoticeDialog(
                onConfirm = {
                    // Clear all data after confirmation
                    sharedViewModel.setDataRetentionAgreed(false)
                    onLogoutClicked(navController, sharedViewModel)
                    showDeletionNoticeDialogOnAutoLogin = false // Dismiss the dialog after confirmation
                },
                onCancel = {
                    // Dismiss the dialog without clearing the data
                    showDeletionNoticeDialogOnAutoLogin = false
                },
                message = "Turning off auto login will delete all your saved login data. Are you sure you want to proceed?"
            )
        }

        // Show the Data Deletion Notice Dialog when logging out
        if (showDeletionNoticeDialogOnLogOut) {
            DataDeletionNoticeDialog(
                onConfirm = {
                    onLogoutClicked(navController, sharedViewModel) // Handle logout
                    showDeletionNoticeDialogOnLogOut = false
                },
                onCancel = {
                    showDeletionNoticeDialogOnLogOut = false
                },
                message = "Logging off will delete all your saved login data. Are you sure you want to proceed?"
            )
        }
    }
}

@Composable
fun DataDeletionNoticeDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    message: String
) {
    AlertDialog(
        onDismissRequest = { /* Dialog will not dismiss on outside clicks */ },
        title = {
            Text(
                text = "Data Deletion Notice!",
                color = Color.Red // Set the title color to red
            )
        },
        text = {
            Text(message)
        },
        confirmButton = {
            Button(onClick = { onConfirm() }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = { onCancel() }) {
                Text("Cancel")
            }
        }
    )
}

private fun onLogoutClicked(navController: NavHostController, sharedViewModel: SharedViewModel) {
    sharedViewModel.clearData() // Clear data on logout
    navController.navigate("login") {
        popUpTo(0) { inclusive = true }
    }
}
