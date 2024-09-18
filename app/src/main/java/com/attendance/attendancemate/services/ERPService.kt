package com.attendance.attendancemate.services

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ERPService {
    @FormUrlEncoded
    @POST("/")
    @Headers(
        "Accept: */*",
        "Accept-Encoding: gzip, deflate, br",
        "Connection: keep-alive",
        "Origin: https://erp.imsuc.ac.in",
        "Referer: https://erp.imsuc.ac.in/",
        "Accept-Language: en-US,en;q=0.9",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36",
        "Host: erp.imsuc.ac.in"
    )
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("/admission/view_schduleAttendence")
    @Headers(
        "Accept: */*",
        "Accept-Encoding: gzip, deflate, br",
        "Connection: keep-alive",
        "Origin: https://erp.imsuc.ac.in",
        "Referer: https://erp.imsuc.ac.in/",
        "Accept-Language: en-US,en;q=0.9",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36",
        "Host: erp.imsuc.ac.in"
    )
    fun getAttendance(
        @Field("selectDate") selectDate: String,
        @Header("Cookie") cookie: String
    ): Call<String>

    @GET("/admission/view_attendance")
    @Headers(
        "Accept: */*",
        "Accept-Encoding: gzip, deflate, br",
        "Connection: keep-alive",
        "Referer: https://erp.imsuc.ac.in/admission",
        "Accept-Language: en-US,en;q=0.9",
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36",
        "Host: erp.imsuc.ac.in"
    )
    fun getAttendanceStats(
        @Header("Cookie") cookie: String
    ): Call<String>
}
