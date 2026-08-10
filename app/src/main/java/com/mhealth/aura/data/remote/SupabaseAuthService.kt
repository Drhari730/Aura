package com.mhealth.aura.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class SupabaseAuthService(
    private val supabaseUrl: String,
    private val anonKey: String
) {
    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() && anonKey.isNotBlank()

    suspend fun sendEmailOtp(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Supabase is not configured for this build.")
            )
        }

        val body = JSONObject()
            .put("email", email)
            .put("create_user", true)

        request(path = "/auth/v1/otp", body = body).map { }
    }

    suspend fun verifyEmailOtp(email: String, token: String): Result<SupabaseSession> =
        withContext(Dispatchers.IO) {
            if (!isConfigured) {
                return@withContext Result.failure(
                    IllegalStateException("Supabase is not configured for this build.")
                )
            }

            val body = JSONObject()
                .put("email", email)
                .put("token", token)
                .put("type", "email")

            request(path = "/auth/v1/verify", body = body).map { json ->
                SupabaseSession(
                    accessToken = json.optString("access_token"),
                    refreshToken = json.optString("refresh_token"),
                    userId = json.optJSONObject("user")?.optString("id").orEmpty()
                )
            }
        }

    private fun request(path: String, body: JSONObject): Result<JSONObject> {
        return runCatching {
            val endpoint = "${supabaseUrl.trimEnd('/')}$path"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
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
                val message = response.extractSupabaseError()
                throw IllegalStateException(
                    message.ifBlank { "Supabase request failed: HTTP $responseCode" }
                )
            }

            if (response.isBlank()) JSONObject() else JSONObject(response)
        }
    }
}

data class SupabaseSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String
)

private fun String.extractSupabaseError(): String {
    return runCatching {
        val json = JSONObject(this)
        json.optString("msg")
            .ifBlank { json.optString("message") }
            .ifBlank { json.optString("error_description") }
            .ifBlank { json.optString("error") }
    }.getOrDefault(this)
}
