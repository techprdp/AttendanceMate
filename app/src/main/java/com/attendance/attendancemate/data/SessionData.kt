package com.attendance.attendancemate.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SessionData(
    val cookie: String,
    val expiry: Long
) : Parcelable
