package com.attendance.attendancemate.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import java.io.InputStream
import java.util.Properties

object CommonUtils {

    private val properties: Properties = Properties()

    fun loadConfig(context :Context) {
        try {
            // Load properties from the assets directory
            val inputStream: InputStream = context.assets.open("config.properties")
            properties.load(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAdMobAdUnitId(): String? {
        return properties.getProperty("ADMOB_AD_BANNER_UNIT_ID")
    }

    // Composable function for AdMob Banner
    @Composable
    fun AdMobBanner(adUnitId: String) {
        val context = LocalContext.current
        val adView = remember { AdView(context) }

        adView.adUnitId = adUnitId
        adView.setAdSize(AdSize.BANNER) // Set the desired ad size here

        LaunchedEffect(Unit) {
            // Load the ad
            adView.loadAd(AdRequest.Builder().build())
        }

        AndroidView(factory = { adView }, update = {
            it.loadAd(AdRequest.Builder().build())
        })
    }

}