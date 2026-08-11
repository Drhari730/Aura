package com.mhealth.aura.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class BrevoOtpService(
    private val apiBaseUrl: String
) {
    val isConfigured: Boolean
        get() = apiBaseUrl.isNotBlank()

    suspend fun sendEmailOtp(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Brevo OTP service is not configured for this build.")
            )
        }

        request(
            path = "/api/auth/request-otp",
            body = JSONObject()
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
                path = "/api/auth/verify-otp",
                body = JSONObject()
                    .put("email", email)
                    .put("code", token)
            ).map { }
        }

    private fun request(path: String, body: JSONObject): Result<JSONObject> {
        return runCatching {
            val endpoint = "${apiBaseUrl.trimEnd('/')}$path"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 20_000
                doOutput = true
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
