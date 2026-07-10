package com.kayansoft.serialadmin.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * يتحدث هذا التطبيق مع نفس السيرفر الذي يعمل عليه تطبيق الكروت بالضبط
 * (kayan-licensing-server.onrender.com) — لضمان أن السيريالات التي يتم
 * إنشاؤها/تعديلها هنا تُقرأ فورًا من نفس مصدر الحقيقة الذي يتحقق منه التطبيق.
 */
interface AdminApiService {

    @POST("api/v1/admin/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/v1/admin/dashboard")
    suspend fun getDashboard(@Header("Authorization") bearerToken: String): Response<DashboardResponse>

    @POST("api/v1/admin/serial/create")
    suspend fun createSerial(
        @Header("Authorization") bearerToken: String,
        @Body request: CreateSerialRequest
    ): Response<CreateSerialResponse>

    @POST("api/v1/admin/serial/reset/{id}")
    suspend fun resetDeviceLock(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: Int
    ): Response<GenericResponse>

    @POST("api/v1/admin/serial/toggle/{id}")
    suspend fun toggleSerialStatus(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: Int
    ): Response<GenericResponse>

    @DELETE("api/v1/admin/serial/delete/{id}")
    suspend fun deleteSerial(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: Int
    ): Response<GenericResponse>
}
