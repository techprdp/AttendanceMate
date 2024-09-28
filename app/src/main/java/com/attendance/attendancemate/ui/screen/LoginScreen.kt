package com.attendance.attendancemate.ui.screen

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.attendance.attendancemate.data.SessionData
import com.attendance.attendancemate.view.LoginViewModel
import com.attendance.attendancemate.view.SharedViewModel
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LoginScreen(navController: NavHostController, sharedViewModel: SharedViewModel = hiltViewModel(), loginViewModel: LoginViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) }
    var email by remember { mutableStateOf(sharedViewModel.email.value ?: "") }
    var password by remember { mutableStateOf(sharedViewModel.password.value ?: "") }
    var selectDate by remember { mutableStateOf(currentDate) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPolicyDialog by remember { mutableStateOf(false) }
    val sessionData by loginViewModel.sessionData.observeAsState()
    val errorMessage by loginViewModel.errorMessage.observeAsState("")
    val isLoading by loginViewModel.isLoading.observeAsState(false)
    val keyboardController = LocalSoftwareKeyboardController.current

    // Retrieve app version name
    val versionName = remember {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName
    }

    // Check session status on composition
    LaunchedEffect(Unit) {
        if (sharedViewModel.sessionData.value != null && sharedViewModel.isSessionExpired()) {
            Toast.makeText(context, "Session Expired. Logging in again...", Toast.LENGTH_SHORT).show()
            loginViewModel.handleLogin(email, password, selectDate)
        } else if (sharedViewModel.sessionData.value != null) {
            // Session is valid, navigate to attendance
            navController.navigate("bottomNav?sessionData=&email=&date=") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Handle successful login
    LaunchedEffect(sessionData) {
        sessionData?.let {
            if (!loginViewModel.isSessionExpired()) {
                if (!sharedViewModel.hasSeenPolicy.value!!) {
                    showPolicyDialog = true
                } else {
                    // Continue saving data based on user preference
                    if (sharedViewModel.dataRetentionAgreed.value == true) {
                        sharedViewModel.setSessionData(it)
                        sharedViewModel.setEmail(email)
                        sharedViewModel.setPassword(password)
                        sharedViewModel.setDate(selectDate)

                        navController.navigate("bottomNav?sessionData=&email=&date=") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    val sessionDataJson = sessionDataToJson(it)
                    navController.navigate("bottomNav?sessionData=$sessionDataJson&email=$email&date=$selectDate") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            } else {
                Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                loginViewModel.handleLogin(email, password, selectDate)
            }
        }
    }

    // Function to show DatePicker
    fun showDatePicker() {
        val today = LocalDate.parse(selectDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        val minDate = LocalDate.of(2024, 8, 20)
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDateTemp = LocalDate.of(year, month + 1, dayOfMonth)
                selectDate = selectedDateTemp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                sharedViewModel.setDate(selectDate)
                showDatePicker = false
            },
            today.year,
            today.monthValue - 1,
            today.dayOfMonth
        )
        datePickerDialog.datePicker.minDate = minDate.toEpochDay() * 86400000
        datePickerDialog.datePicker.maxDate = LocalDate.now().toEpochDay() * 86400000

        datePickerDialog.setOnDismissListener {
            showDatePicker = false
        }
        datePickerDialog.show()
    }

    // Show the Data Retention Policy Dialog if needed
    if (showPolicyDialog) {
        DataRetentionPolicyDialog(
            onAgree = {
                sessionData?.let {
                    sharedViewModel.setSessionData(it)
                    sharedViewModel.setEmail(email)
                    sharedViewModel.setPassword(password)
                    sharedViewModel.setDate(selectDate)
                    sharedViewModel.setDataRetentionAgreed(true)

                    navController.navigate("bottomNav?sessionData=&email=&date=") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                sharedViewModel.setHasSeenPolicy(true)
                showPolicyDialog = false
            },
            onDisagree = {
                // Do not save data, but still navigate with sessionData
                sessionData?.let {
                    sharedViewModel.setDataRetentionAgreed(false)

                    val sessionDataJson = sessionDataToJson(it)
                    navController.navigate("bottomNav?sessionData=$sessionDataJson&email=$email&date=$selectDate") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                sharedViewModel.setHasSeenPolicy(true)
                showPolicyDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Login form centered in the Box
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Admission ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = selectDate,
                onValueChange = {},
                label = { Text("Select Date") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker() },
                enabled = false
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        loginViewModel.handleLogin(email, password, selectDate)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
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
    }
}

fun sessionDataToJson(sessionData: SessionData): String {
    return Gson().toJson(sessionData)
}

@Composable
fun DataRetentionPolicyDialog(
    onAgree: () -> Unit,
    onDisagree: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Dialog will not dismiss on outside clicks */ },
        title = { Text(text = "Would you like to save your login data?") },
        text = {
            Text("We will retain your login data locally on the device to make future logins easier, without sharing any data off the device. You can always manage this setting later within the app. Do you agree?")
        },
        confirmButton = {
            Button(onClick = { onAgree() }) {
                Text("Agree")
            }
        },
        dismissButton = {
            Button(onClick = { onDisagree() }) {
                Text("Disagree")
            }
        }
    )
}

