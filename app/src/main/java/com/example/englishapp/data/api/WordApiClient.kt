package com.example.englishapp.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Async client for xxapi.cn English word APIs.
 *
 * Uses built-in HttpURLConnection + kotlinx.coroutines — no extra dependencies.
 * All public methods are suspend functions that run on [Dispatchers.IO].
 */
object WordApiClient {

    private const val BASE = "https://v2.xxapi.cn/api"

    /**
     * Fetch detailed info for a specific English word.
     * GET /api/englishwords?word={word}
     */
    suspend fun fetchWordDetail(word: String): WordApiResponse = withContext(Dispatchers.IO) {
        val encoded = java.net.URLEncoder.encode(word.trim(), "UTF-8")
        httpGet("$BASE/englishwords?word=$encoded")
    }

    /**
     * Fetch a random daily English word / sentence.
     * GET /api/randomenglishwords
     */
    suspend fun fetchRandomWord(): WordApiResponse = withContext(Dispatchers.IO) {
        httpGet("$BASE/randomenglishwords")
    }

    // ── low-level HTTP helper ──────────────────────────────────────

    private fun httpGet(url: String): WordApiResponse {
        var conn: HttpURLConnection? = null
        return try {
            conn = URL(url).openConnection() as HttpURLConnection
            conn.apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
            }
            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            val body = reader.readText()
            reader.close()
            parseWordApiResponse(body)
        } catch (e: Exception) {
            WordApiResponse(code = -1, msg = e.message ?: "Network error", data = null, requestId = null)
        } finally {
            conn?.disconnect()
        }
    }
}
