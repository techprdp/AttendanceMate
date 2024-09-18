package com.attendance.attendancemate.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.attendancemate.data.SessionData
import com.attendance.attendancemate.model.Subject
import com.attendance.attendancemate.services.MockApiService
import com.attendance.attendancemate.services.RetrofitInstance
import kotlinx.coroutines.launch
import org.json.Cookie
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class AttendanceViewModel : ViewModel() {
    private val _subjects = MutableLiveData<List<Subject>>()
    val subjects: LiveData<List<Subject>> get() = _subjects

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _attendancePercentage = MutableLiveData<String>()
    val attendancePercentage: LiveData<String?> = _attendancePercentage

    private val _attendanceTotal = MutableLiveData<String>()
    val attendanceTotal: LiveData<String?> = _attendanceTotal

    private val _attendancePresent = MutableLiveData<String>()
    val attendancePresent: LiveData<String?> = _attendancePresent

    private val _attendanceAbsent = MutableLiveData<String>()
    val attendanceAbsent: LiveData<String?> = _attendanceAbsent

    private val _studentName = MutableLiveData<String>()
    val studentName: LiveData<String?> = _studentName

    private val _attendanceLoaded = MutableLiveData(false)
    val attendanceLoaded: LiveData<Boolean> get() = _attendanceLoaded

    private val _attendancePercentageLoaded = MutableLiveData(false)
    val attendancePercentageLoaded: LiveData<Boolean> get() = _attendancePercentageLoaded

    // Method to check if session is expired
    fun isSessionExpired(sessionExpiry: Long): Boolean {
        return sessionExpiry < System.currentTimeMillis()
    }

    fun fetchAttendance(sessionData: SessionData, selectDate: String, email: String) {
        viewModelScope.launch {
            if (isSessionExpired(sessionData.expiry)) {
                _errorMessage.value = "Session expired. Please log in again."
                _attendanceLoaded.value = false
                return@launch
            }

            val cookieHeader = sessionData.cookie

            if (email == "1234") {
                // not for production code
                _subjects.value = MockApiService.getAttendance()
                _errorMessage.value = null
                _attendanceLoaded.value = true
            } else {
                RetrofitInstance.api.getAttendance(selectDate, cookieHeader).enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        handleAttendanceResponse(response, sessionData, email)
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        handleApiException(t)
                    }
                })
            }
        }
    }

    private fun handleAttendanceResponse(response: Response<String>, sessionData: SessionData, email: String) {
        if (response.isSuccessful) {
            val attendanceResponse = response.body()
            if (attendanceResponse != null) {
                try {
                    val jsonArray = JSONArray(attendanceResponse)
                    try {
                        val innerJsonArray =
                            JSONArray(jsonArray.get(1).toString())
                        val subjectsList = mutableListOf<Subject>()

                        for (i in 0 until innerJsonArray.length()) {
                            val jsonObject = innerJsonArray.getJSONObject(i)
                            subjectsList.add(
                                Subject(
                                    jsonObject.getString("subject_name"),
                                    jsonObject.getString("subject_code"),
                                    jsonObject.getString("room_no"),
                                    jsonObject.getString("faculty_name"),
                                    jsonObject.getString("attendence")
                                )
                            )
                        }
                        _subjects.value = subjectsList
                        _errorMessage.value = null

                        // Fetch attendance percentage
                        fetchAttendancePercentage(sessionData, email)

                        _attendanceLoaded.value = true
                    } catch (e: JSONException) {
                        _errorMessage.value =
                            if (jsonArray.length() > 1) {
                                jsonArray.get(1).toString()
                            } else {
                                "Parsing error: ${e.message}"
                            }
                        fetchAttendancePercentage(sessionData, email)
                        _attendanceLoaded.value = false
                    }
                } catch (e: Exception) {
                    _errorMessage.value =
                        "Attendance request failed: Please try after sometime"
                    _attendanceLoaded.value = false
                }
            } else {
                _errorMessage.value =
                    "Attendance request failed: ${response.code()}"
                _attendanceLoaded.value = false
            }
        } else {
            _errorMessage.value =
                "Attendance request failed: ${response.code()}"
            _attendanceLoaded.value = false
        }
    }

    private fun handleApiException(exception: Throwable) {
        when (exception) {
            is IOException -> _errorMessage.value = "Network error: ${exception.message}"
            is JSONException -> _errorMessage.value = "Parsing error: ${exception.message}"
            else -> _errorMessage.value = "Unknown error: ${exception.message}"
        }
        _attendanceLoaded.value = false
    }

    private fun fetchAttendancePercentage(sessionData: SessionData, email: String) {
        viewModelScope.launch {
            if (isSessionExpired(sessionData.expiry)) {
                _errorMessage.value = "Session expired. Please log in again."
                _attendancePercentageLoaded.value = false
                return@launch
            }

            if (email == "1234") {
                // not for production code
                _studentName.value = MockApiService.getName()
                _attendanceTotal.value = "0"
                _attendancePresent.value = "0"
                _attendanceAbsent.value = "0"
                _attendancePercentage.value = "0"
                _attendancePercentageLoaded.value = true
            } else {
                RetrofitInstance.api.getAttendanceStats(sessionData.cookie).enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        handleAttendancePercentageResponse(response)
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        handleApiException(t)
                    }
                })
            }
        }
    }

    private fun handleAttendancePercentageResponse(response: Response<String>) {
        if (response.isSuccessful) {
            val responseBody = response.body()
            if (responseBody != null) {
                try {
                    val document = Jsoup.parse(responseBody)
                    val element = document.getElementsByClass("box-messages")
                    val attendanceObject = element.attr("ng-init")

                    if (attendanceObject.isNotEmpty() &&
                        attendanceObject.contains("attendance_in_percentage") &&
                        attendanceObject.contains("total_attendance") &&
                        attendanceObject.contains("present_attendance") &&
                        attendanceObject.contains("absent_attendance") &&
                        attendanceObject.contains("first_name")
                    ) {

                        val attendanceDataSplit = attendanceObject.split(";")
                        val attendanceDataValue = attendanceDataSplit[1]
                        val attendanceDataValueArray =
                            attendanceDataValue.split("=")
                        val attendanceJSONObject =
                            JSONObject(attendanceDataValueArray[1])
                        val studentNameValue = attendanceDataSplit[0]
                        val studentNameValueArray = studentNameValue.split("=")
                        val studentNameJSONObject =
                            JSONObject(studentNameValueArray[1])

                        _studentName.value = toCamelCase(
                            studentNameJSONObject.getString("first_name").toString()
                        )
                        _attendanceTotal.value =
                            attendanceJSONObject.getInt("total_attendance")
                                .toString()
                        _attendancePresent.value =
                            attendanceJSONObject.getInt("present_attendance")
                                .toString()
                        _attendanceAbsent.value =
                            attendanceJSONObject.getInt("absent_attendance")
                                .toString()
                        _attendancePercentage.value =
                            attendanceJSONObject.getDouble("attendance_in_percentage")
                                .toString()
                        _attendancePercentageLoaded.value = true
                    } else {
                        _errorMessage.value =
                            "Error fetching attendance summary."
                        _attendancePercentageLoaded.value = false
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Parsing error: ${e.message}"
                    _attendancePercentageLoaded.value = false
                }
            } else {
                _errorMessage.value =
                    "Attendance summary request failed: ${response.code()}"
                _attendancePercentageLoaded.value = false
            }
        } else {
            _errorMessage.value =
                "Attendance summary request failed: ${response.code()}"
            _attendancePercentageLoaded.value = false
        }
    }

    private fun toCamelCase(input: String): String {
        return input
            .lowercase()
            .split(" ", "_", "-") // Split by space, underscore, or hyphen
            .filter { it.isNotEmpty() }
            .joinToString("") { word ->
                word.replaceFirstChar { it.uppercase() } // Capitalize each word
            }
    }
}

