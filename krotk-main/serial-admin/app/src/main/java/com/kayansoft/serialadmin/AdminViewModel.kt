package com.kayansoft.serialadmin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kayansoft.serialadmin.network.Client
import com.kayansoft.serialadmin.network.CreateSerialRequest
import com.kayansoft.serialadmin.network.LoginRequest
import com.kayansoft.serialadmin.network.LogEntry
import com.kayansoft.serialadmin.network.RetrofitClient
import com.kayansoft.serialadmin.network.Serial
import com.kayansoft.serialadmin.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object CheckingSession : AuthState()
    object LoggedOut : AuthState()
    object LoggedIn : AuthState()
}

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.api

    private var token: String? = null

    private val _authState = MutableStateFlow<AuthState>(AuthState.CheckingSession)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    private val _serials = MutableStateFlow<List<Serial>>(emptyList())
    val serials: StateFlow<List<Serial>> = _serials.asStateFlow()

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = tokenManager.getToken()
            if (saved != null) {
                token = saved
                _authState.value = AuthState.LoggedIn
                refreshDashboard()
            } else {
                _authState.value = AuthState.LoggedOut
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = api.login(LoginRequest(username, password))
                val body = response.body()
                if (response.isSuccessful && body?.success == true && body.token != null) {
                    token = body.token
                    tokenManager.saveToken(body.token)
                    _authState.value = AuthState.LoggedIn
                    refreshDashboard()
                } else {
                    _errorMessage.value = body?.message ?: "فشل تسجيل الدخول"
                }
            } catch (e: Exception) {
                _errorMessage.value = "تعذر الاتصال بالسيرفر: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            token = null
            _clients.value = emptyList()
            _serials.value = emptyList()
            _logs.value = emptyList()
            _authState.value = AuthState.LoggedOut
        }
    }

    private fun authHeader(): String? = token?.let { "Bearer $it" }

    fun refreshDashboard() {
        val header = authHeader() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getDashboard(header)
                if (response.code() == 401 || response.code() == 403) {
                    handleSessionExpired()
                    return@launch
                }
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _clients.value = body.clients
                    _serials.value = body.serials
                    _logs.value = body.logs
                } else {
                    _errorMessage.value = body?.message ?: "تعذر تحميل البيانات"
                }
            } catch (e: Exception) {
                _errorMessage.value = "تعذر الاتصال بالسيرفر: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleSessionExpired() {
        viewModelScope.launch {
            tokenManager.clearToken()
            token = null
            _authState.value = AuthState.LoggedOut
            _errorMessage.value = "انتهت صلاحية الجلسة، الرجاء تسجيل الدخول مرة أخرى"
        }
    }

    fun createSerial(
        name: String,
        networkName: String,
        phone: String,
        durationMonths: Int,
        notes: String,
        deviceId: String
    ) {
        val header = authHeader() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = api.createSerial(
                    header,
                    CreateSerialRequest(
                        name = name,
                        networkName = networkName,
                        phone = phone,
                        durationMonths = durationMonths,
                        notes = notes.ifBlank { null },
                        deviceId = deviceId.ifBlank { null }
                    )
                )
                val body = response.body()
                if (response.isSuccessful && body?.success == true && body.serial != null) {
                    _successMessage.value = "تم إنشاء السيريال: ${body.serial.serialKey}"
                    refreshDashboard()
                } else {
                    _errorMessage.value = body?.message ?: "تعذر إنشاء السيريال"
                }
            } catch (e: Exception) {
                _errorMessage.value = "تعذر الاتصال بالسيرفر: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSerial(id: Int) {
        val header = authHeader() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.toggleSerialStatus(header, id)
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _successMessage.value = body.message
                    refreshDashboard()
                } else {
                    _errorMessage.value = body?.message ?: "تعذر تنفيذ العملية"
                }
            } catch (e: Exception) {
                _errorMessage.value = "تعذر الاتصال بالسيرفر: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetDeviceLock(id: Int) {
        val header = authHeader() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.resetDeviceLock(header, id)
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _successMessage.value = body.message
                    refreshDashboard()
                } else {
                    _errorMessage.value = body?.message ?: "تعذر تنفيذ العملية"
                }
            } catch (e: Exception) {
                _errorMessage.value = "تعذر الاتصال بالسيرفر: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSerial(id: Int) {
        val header = authHeader() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.deleteSerial(header, id)
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _successMessage.value = body.message
                    refreshDashboard()
                } else {
                    _errorMessage.value = body?.message ?: "تعذر تنفيذ العملية"
                }
            } catch (e: Exception) {
                _errorMessage.value = "تعذر الاتصال بالسيرفر: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
