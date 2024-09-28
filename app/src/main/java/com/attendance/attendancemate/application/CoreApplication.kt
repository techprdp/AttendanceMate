package com.attendance.attendancemate.application

import android.app.Application
import com.attendance.attendancemate.utils.CommonUtils
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CoreApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        CommonUtils.loadConfig(this)
        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this) { }
    }
}