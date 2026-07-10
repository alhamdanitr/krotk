package com.kayansoft.serialadmin.network

import com.squareup.moshi.Json

data class LoginRequest(val username: String, val password: String)

data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val message: String? = null
)

data class Client(
    val id: Int,
    val name: String,
    @Json(name = "network_name") val networkName: String,
    val phone: String,
    val notes: String? = null
)

data class Serial(
    val id: Int,
    @Json(name = "client_id") val clientId: Int,
    @Json(name = "serial_key") val serialKey: String,
    @Json(name = "device_id") val deviceId: String? = null,
    @Json(name = "duration_months") val durationMonths: Int,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    val status: String, // ACTIVE, UNUSED, REVOKED
    val notes: String? = null
)

data class LogEntry(
    val ip: String? = null,
    val endpoint: String? = null,
    val status: Int? = null,
    val message: String? = null,
    val timestamp: String? = null
)

data class DashboardResponse(
    val success: Boolean,
    val clients: List<Client> = emptyList(),
    val serials: List<Serial> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val message: String? = null
)

data class CreateSerialRequest(
    val name: String,
    @Json(name = "network_name") val networkName: String,
    val phone: String,
    @Json(name = "duration_months") val durationMonths: Int,
    val notes: String? = null,
    @Json(name = "device_id") val deviceId: String? = null
)

data class CreateSerialResponse(
    val success: Boolean,
    val serial: Serial? = null,
    val message: String? = null
)

data class GenericResponse(
    val success: Boolean,
    val message: String? = null
)
