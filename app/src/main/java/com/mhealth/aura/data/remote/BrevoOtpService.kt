package com.mhealth.aura.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class BrevoOtpService(
    private val supabaseUrl: String,
    private val anonKey: String,
    private val functionName: String
) {
    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() && anonKey.isNotBlank() && functionName.isNotBlank()

    suspend fun sendEmailOtp(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Brevo OTP service is not configured for this build.")
            )
        }

        request(
            JSONObject()
                .put("action", "send")
                .put("email", email)
        ).map { }
    }

    suspend fun verifyEmailOtp(email: String, token: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!isConfigured) {
                return@withContext Result.failure(
                    IllegalStateException("Brevo OTP service is not configured for this build.")
                )
            }

            request(
                JSONObject()
                    .put("action", "verify")
                    .put("email", email)
                    .put("code", token)
            ).map { }
        }

    private fun request(body: JSONObject): Result<JSONObject> {
        return runCatching {
            val endpoint = "${supabaseUrl.trimEnd('/')}/functions/v1/$functionName"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 20_000
                doOutput = true
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
                setRequestProperty("Content-Type", "application/json")
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (responseCode !in 200..299) {
                val message = response.extractApiError()
                throw IllegalStateException(
                    message.ifBlank { "Brevo OTP request failed: HTTP $responseCode" }
                )
            }

            if (response.isBlank()) JSONObject() else JSONObject(response)
        }
    }
}

private fun String.extractApiError(): String {
    return runCatching {
        val json = JSONObject(this)
        json.optString("error")
            .ifBlank { json.optString("message") }
            .ifBlank { json.optString("reason") }
    }.getOrDefault(this)
}
