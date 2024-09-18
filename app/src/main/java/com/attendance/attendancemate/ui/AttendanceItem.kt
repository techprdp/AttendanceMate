package com.attendance.attendancemate.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.attendance.attendancemate.model.Subject

@Composable
fun AttendanceItem(subject: Subject) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text("Subject Name: ${subject.subject_name}")
        Text("Subject Code: ${subject.subject_code}")
        Text("Room No.: ${subject.room_no}")
        Text("Faculty Name: ${subject.faculty_name}")
        Text("Attendance: ${subject.attendence}")
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}