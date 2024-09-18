package com.attendance.attendancemate.ui.screen

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.attendance.attendancemate.ui.AttendanceItem
import com.attendance.attendancemate.view.AttendanceViewModel
import com.attendance.attendancemate.view.SharedViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    navController: NavHostController,
    attendanceViewModel: AttendanceViewModel = viewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sessionData by sharedViewModel.sessionData.observeAsState()
    val email by sharedViewModel.email.observeAsState()
    val date by sharedViewModel.date.observeAsState()
    val subjects by attendanceViewModel.subjects.observeAsState(emptyList())
    val errorMessage by attendanceViewModel.errorMessage.observeAsState(null)
    val attendancePercentage by attendanceViewModel.attendancePercentage.observeAsState("")
    val attendanceTotal by attendanceViewModel.attendanceTotal.observeAsState("")
    val attendancePresent by attendanceViewModel.attendancePresent.observeAsState("")
    val attendanceAbsent by attendanceViewModel.attendanceAbsent.observeAsState("")
    val studentName by attendanceViewModel.studentName.observeAsState("Buddy")
    val attendanceLoaded by attendanceViewModel.attendanceLoaded.observeAsState(false)
    val attendancePercentageLoaded by attendanceViewModel.attendancePercentageLoaded.observeAsState(false)
    var isDropdownMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val currentDate = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) }
    val selectedDate = date ?: currentDate

    // Launch data fetching when the view is composed or date changes
    LaunchedEffect(sessionData, date, email) {
        sessionData?.let { data ->
            if (!attendanceViewModel.isSessionExpired(data.expiry)) {
                attendanceViewModel.fetchAttendance(data, date ?: "", email ?: "")
                Log.d("attendanceScreen", "launched")
            } else {
                // Handle session expiration
                Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                onLogoutClicked(navController, sharedViewModel)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hi, ${studentName}!") },
                actions = {
                    IconButton(onClick = { isDropdownMenuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More Options")
                    }

                    DropdownMenu(
                        expanded = isDropdownMenuExpanded,
                        onDismissRequest = { isDropdownMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                onLogoutClicked(navController, sharedViewModel)
                                isDropdownMenuExpanded = false
                            }
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    val error = errorMessage
                    if (error != null) {
                        Text(error, color = MaterialTheme.colorScheme.error)
                    } else if (!attendanceLoaded || !attendancePercentageLoaded) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (subjects.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(subjects) { subject ->
                                AttendanceItem(subject = subject)
                            }
                        }
                    } else {
                        Text("No attendance data available.")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Attendance summary card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    FloatingActionButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        val formattedDate =
                            LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                .let { date ->
                                    val day = date.dayOfMonth
                                    val month = date.month.getDisplayName(
                                        java.time.format.TextStyle.SHORT,
                                        Locale.getDefault()
                                    )
                                    "$day${getDaySuffix(day)} $month"
                                }
                        Text(
                            text = formattedDate,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.elevatedCardElevation(),
                        colors = CardDefaults.elevatedCardColors()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Attendance Summary",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Total Classes: $attendanceTotal")
                            Text(text = "Classes Attended: $attendancePresent")
                            Text(text = "Classes Absent: $attendanceAbsent")
                            Text(text = "Attendance Percentage: $attendancePercentage%")
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            val today = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            val minDate = LocalDate.of(2024, 8, 20)
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDateTemp = LocalDate.of(year, month + 1, dayOfMonth)
                    sharedViewModel.setDate(selectedDateTemp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
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
    }
}

private fun onLogoutClicked(navController: NavHostController, sharedViewModel: SharedViewModel) {
    sharedViewModel.clearData()
    navController.navigate("login") {
        popUpTo("attendance") { inclusive = true }
    }
}

private fun getDaySuffix(day: Int): String {
    return when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}
