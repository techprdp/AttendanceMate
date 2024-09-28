package com.attendance.attendancemate.ui.screen

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.attendance.attendancemate.data.SessionData
import com.attendance.attendancemate.ui.AttendanceItem
import com.attendance.attendancemate.view.AttendanceViewModel
import com.attendance.attendancemate.view.SharedViewModel
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AttendanceScreen(
    navController: NavHostController,
    paramSessionData: String?,
    email: String?,
    date: String?,
    attendanceViewModel: AttendanceViewModel = viewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sharedSessionData by sharedViewModel.sessionData.observeAsState()
    // Decode the JSON string into SessionData object using Gson
    val gson = Gson()
    val currentSession = paramSessionData?.let {
        try {
            gson.fromJson(it, SessionData::class.java)
        } catch (e: Exception) {
            null
        }
    } ?: sharedSessionData

    val sharedEmail by sharedViewModel.email.observeAsState()
    val sharedDate by sharedViewModel.date.observeAsState()
    val subjects by attendanceViewModel.subjects.observeAsState(emptyList())
    val errorMessage by attendanceViewModel.errorMessage.observeAsState(null)
    val attendancePercentage by attendanceViewModel.attendancePercentage.observeAsState("")
    val attendanceTotal by attendanceViewModel.attendanceTotal.observeAsState("")
    val attendancePresent by attendanceViewModel.attendancePresent.observeAsState("")
    val attendanceAbsent by attendanceViewModel.attendanceAbsent.observeAsState("")
    val studentName by attendanceViewModel.studentName.observeAsState("Buddy")
    val attendanceLoaded by attendanceViewModel.attendanceLoaded.observeAsState(false)
    val attendancePercentageLoaded by attendanceViewModel.attendancePercentageLoaded.observeAsState(false)
    var showDatePicker by remember { mutableStateOf(false) }
    val userEmail = email?.takeIf { it.isNotEmpty() } ?: sharedEmail
    val selectedDate = date?.takeIf { it.isNotEmpty() } ?: sharedDate

    // Launch data fetching when the view is composed or date changes
    LaunchedEffect(currentSession, selectedDate, userEmail) {
        currentSession?.let { data ->
            if (!attendanceViewModel.isSessionExpired(data.expiry)) {
                attendanceViewModel.fetchAttendance(data, selectedDate ?: "", userEmail ?: "")
                Log.d("attendanceScreen", "launched")
            } else {
                // Handle session expiration
                if (sharedViewModel.dataRetentionAgreed.value != true)
                    Toast.makeText(context, "Session Expired. Please login again.", Toast.LENGTH_SHORT).show()
               navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title inside the main content
            Text(
                text = "Hi, $studentName!",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            // Add space between the title and the list
            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            // Attendance summary card
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.elevatedCardColors()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Attendance Summary",
                            style = MaterialTheme.typography.titleMedium
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

        // Floating Action Button at the bottom right
        FloatingActionButton(
            onClick = { showDatePicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
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
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
        }

        // Date picker dialog logic...
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

private fun getDaySuffix(day: Int): String {
    return when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}
