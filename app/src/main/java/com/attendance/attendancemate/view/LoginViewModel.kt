package com.attendance.attendancemate.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.attendancemate.data.SessionData
import com.attendance.attendancemate.services.RetrofitInstance
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private val _sessionData = MutableLiveData<SessionData?>()
    val sessionData: LiveData<SessionData?> get() = _sessionData

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun handleLogin(email: String, password: String, selectDate: String) {
        if (email.isEmpty() || password.isEmpty() || selectDate.isEmpty()) {
            _errorMessage.value = "Provided details are not valid"
            return
        }

        if (email == "1234") {
            val eightHoursInMills =  60 * 1000
            // test credential
            _sessionData.value = SessionData("cookieHeader", System.currentTimeMillis() + eightHoursInMills)
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            RetrofitInstance.api.login(email, password).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    _isLoading.value = false
                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body()?.string()
                            if (responseBody != null) {
                                val document: Document = Jsoup.parse(responseBody)
                                val strongTag = document.select("strong").first()

                                if (strongTag != null) {
                                    val extractedEmail = strongTag.text()
                                    if (extractedEmail.startsWith(email)) {
                                        val cookies = RetrofitInstance.getCookies()
                                        val cookieHeader = cookies.joinToString("; ") {
                                            "${it.name}=${it.value}"
                                        }
                                        val cookieExpiry = cookies.firstOrNull()?.expiresAt ?: 0

                                        // Success, update login result
                                        _sessionData.value = SessionData(cookieHeader, cookieExpiry)

                                    } else {
                                        _errorMessage.value =
                                            "Login failed: Incorrect admission ID/password"
                                    }
                                } else {
                                    _errorMessage.value =
                                        "Login failed: Incorrect admission ID/password"
                                }
                            } else {
                                _errorMessage.value = "Login failed: Unable to parse response"
                            }
                        } else {
                            _errorMessage.value = "Login failed: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        _errorMessage.value = "Login failed: ${e.message}"
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "Network error: ${t.message}"
                }
            })
        }
    }

    fun isSessionExpired(): Boolean {
        val sessionData = _sessionData.value ?: return true
        return System.currentTimeMillis() > sessionData.expiry
    }
}
