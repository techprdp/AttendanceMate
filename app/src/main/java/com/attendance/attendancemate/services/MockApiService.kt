package com.attendance.attendancemate.services

import com.attendance.attendancemate.model.Subject

object MockApiService {

    // Simulated method to fetch attendance
    fun getAttendance(): List<Subject> {
        // Mock data
        return listOf(
            Subject(
                subject_name = "Mathematics",
                subject_code = "MTH101",
                room_no = "101",
                faculty_name = "Dr. Smith",
                attendence = "Present"
            ),
            Subject(
                subject_name = "Physics",
                subject_code = "PHY102",
                room_no = "202",
                faculty_name = "Prof. Johnson",
                attendence = "Absent"
            )
        )
    }

    fun getName(): String {
        return "User"
    }
}
