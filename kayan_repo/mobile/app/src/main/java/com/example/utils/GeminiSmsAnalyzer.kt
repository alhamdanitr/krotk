package com.example.utils

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object GeminiSmsAnalyzer {

    @JsonClass(generateAdapter = true)
    data class GeminiRequest(
        val contents: List<Content>,
        val generationConfig: GenerationConfig? = null
    )

    @JsonClass(generateAdapter = true)
    data class Content(
        val parts: List<Part>
    )

    @JsonClass(generateAdapter = true)
    data class Part(
        val text: String
    )

    @JsonClass(generateAdapter = true)
    data class GenerationConfig(
        val responseMimeType: String? = null
    )

    @JsonClass(generateAdapter = true)
    data class ExtractedCard(
        val code: String,
        val category: Int,
        val username: String? = null,
        val password: String? = null,
        val expiryDate: String? = null
    )

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun analyzeSms(messageText: String, apiKey: String): List<ExtractedCard> = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty()) {
            throw IllegalArgumentException("مفتاح Gemini API فارغ. يرجى إدخاله في الإعدادات.")
        }

        val prompt = """
            تحليل محتوى رسالة الـ SMS التالية واستخراج كروت الشحن، والتي قد تتكون من رقم كود عادي أو (اسم مستخدم وكلمة مرور):
            1. رقم كود كرت الشحن (رقم الكرت أو كود الشحن PIN Code) ومصاحباته.
            2. قيمة الكرت (الفئة 100، 200، 250، 300، 500)
            3. تاريخ انتهاء الصلاحية (إن وجد، كـ نص مثل YYYY-MM-DD أو YYYY/MM)

            يجب أن تعيد النتيجة كـ مصفوفة JSON حصراً بالتنسيق التالي:
            [
              {
                "code": "رقم الكود المستخرج بالكامل أو اسم مستخدم وكلمة مرور مدمجين",
                "username": "اسم المستخدم إن وجد بشكل منفصل، وإلا نفس الكود",
                "password": "كلمة المرور/السر إن وجدت بشكل منفصل، وإلا null أو نص فارغ",
                "category": 100,
                "expiryDate": "تاريخ الصلاحية أو null إذا لم يوجد"
              }
            ]
            قواعد صارمة:
            - لا تضف أي نص خارج مصفوفة الـ JSON المرجعة.
            - يجب استخراج كافة الكروت الموجودة في رسالة الـ SMS بشكل دقيق جداً ومنع دمج أو اقتصاص أي رقم كود للكرت.
            - قيمة الكرت (category) يجب أن تكون من الفئات المدعومة (100، 200، 250، 300، 500) بناء على سياق النص، أو خمن الفئة الأقرب إذا كانت واضحة.
            - إذا لم تجد كروت في النص أرجع مصفوفة فارغة [].

            نص رسالة الـ SMS المطلوب تحليلها:
            $messageText
        """.trimIndent()

        val requestPayload = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(responseMimeType = "application/json")
        )

        val requestAdapter = moshi.adapter(GeminiRequest::class.java)
        val jsonString = requestAdapter.toJson(requestPayload)

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonString.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorMsg = response.body?.string() ?: ""
                    Log.e("GeminiSmsAnalyzer", "API response failed code: ${response.code}, message: $errorMsg")
                    throw Exception("فشل الاتصال بـ Gemini API: رمز خطأ ${response.code}")
                }

                val responseBodyStr = response.body?.string() ?: ""
                Log.d("GeminiSmsAnalyzer", "Response: $responseBodyStr")
                
                // Parse standard Gemini response structure
                val rootJson = moshi.adapter(Map::class.java).fromJson(responseBodyStr) as? Map<*, *>
                val candidates = rootJson?.get("candidates") as? List<*>
                val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
                val content = firstCandidate?.get("content") as? Map<*, *>
                val parts = content?.get("parts") as? List<*>
                val firstPart = parts?.firstOrNull() as? Map<*, *>
                val rawText = firstPart?.get("text") as? String ?: "[]"

                Log.d("GeminiSmsAnalyzer", "Extracted raw JSON text: $rawText")

                val cleanText = rawText.trim()
                val listType = Types.newParameterizedType(List::class.java, ExtractedCard::class.java)
                val listAdapter = moshi.adapter<List<ExtractedCard>>(listType)
                return@withContext listAdapter.fromJson(cleanText) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("GeminiSmsAnalyzer", "Error analyzing SMS content", e)
            throw e
        }
    }
}
