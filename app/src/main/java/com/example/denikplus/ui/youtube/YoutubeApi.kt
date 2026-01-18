// FILE: ui/youtube/YouTubeApi.kt
package com.example.denikplus.ui.youtube

import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class YouTubeVideo(
    val videoId: String,
    val title: String,
    val channel: String,
    val thumbUrl: String
)

object YouTubeApi {

    fun search(apiKey: String, query: String, maxResults: Int = 12): List<YouTubeVideo> {
        val q = Uri.encode(query)
        val url = URL(
            "https://www.googleapis.com/youtube/v3/search" +
                    "?part=snippet&type=video&maxResults=$maxResults&q=$q&key=$apiKey"
        )

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
        }

        val body = try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            stream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }

        val json = JSONObject(body)
        val items = json.optJSONArray("items") ?: return emptyList()

        val out = ArrayList<YouTubeVideo>()
        for (i in 0 until items.length()) {
            val it = items.getJSONObject(i)
            val id = it.optJSONObject("id") ?: continue
            val videoId = id.optString("videoId", "")
            if (videoId.isBlank()) continue

            val sn = it.optJSONObject("snippet") ?: continue
            val title = sn.optString("title", "").ifBlank { "Bez názvu" }
            val channel = sn.optString("channelTitle", "")
            val thumbs = sn.optJSONObject("thumbnails")
            val medium = thumbs?.optJSONObject("medium")
            val thumbUrl = medium?.optString("url", "")?.ifBlank {
                "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
            } ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

            out += YouTubeVideo(
                videoId = videoId,
                title = title,
                channel = channel,
                thumbUrl = thumbUrl
            )
        }
        return out
    }

    fun defaultThumb(videoId: String): String =
        "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
}
