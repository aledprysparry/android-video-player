package com.aledparry.player

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

/**
 * Fetches and parses an IPTV-style M3U playlist (the `#EXTINF` format used by
 * iptv-org, Free-TV/IPTV, etc.) into a list of [Channel]s.
 *
 * This is NOT an HLS media-playlist parser — a single live stream (`.m3u8` with
 * `#EXT-X-*` tags) is handled directly by ExoPlayer. [looksLikeHlsManifest] tells
 * the two apart so the UI can route the URL correctly.
 */
object M3uParser {

    /** Result of loading a URL the user pasted. */
    sealed interface LoadResult {
        data class Playlist(val channels: List<Channel>) : LoadResult
        /** A single live stream — hand straight to the player. */
        data object SingleStream : LoadResult
        data class Error(val message: String) : LoadResult
    }

    private val EXTINF_ATTR = Regex("""([\w-]+)="([^"]*)"""")

    fun load(rawUrl: String): LoadResult {
        val url = rawUrl.trim()
        if (url.isEmpty()) return LoadResult.Error("Enter a playlist URL")
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return LoadResult.Error("URL must start with http:// or https://")
        }

        val text = try {
            fetch(url)
        } catch (e: Exception) {
            return LoadResult.Error("Couldn't load playlist: ${e.message ?: "network error"}")
        }

        if (looksLikeHlsManifest(text)) return LoadResult.SingleStream

        val channels = parse(text)
        return if (channels.isEmpty()) {
            LoadResult.Error("No channels found in that playlist")
        } else {
            LoadResult.Playlist(channels)
        }
    }

    /** A live HLS manifest has stream/segment tags an IPTV channel list never does. */
    fun looksLikeHlsManifest(text: String): Boolean {
        return text.contains("#EXT-X-STREAM-INF") ||
            text.contains("#EXT-X-TARGETDURATION") ||
            text.contains("#EXT-X-MEDIA-SEQUENCE") ||
            text.contains("#EXT-X-ENDLIST")
    }

    fun parse(text: String): List<Channel> {
        val channels = ArrayList<Channel>()
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }

        var pendingName: String? = null
        var pendingLogo: String? = null
        var pendingGroup: String? = null

        for (line in lines) {
            when {
                line.startsWith("#EXTINF") -> {
                    val attrs = EXTINF_ATTR.findAll(line).associate { it.groupValues[1] to it.groupValues[2] }
                    pendingLogo = attrs["tvg-logo"]?.takeIf { it.isNotBlank() }
                    pendingGroup = attrs["group-title"]?.takeIf { it.isNotBlank() }
                    // Display name is the text after the final comma.
                    pendingName = line.substringAfterLast(',', "").trim().ifEmpty {
                        attrs["tvg-name"] ?: attrs["tvg-id"] ?: "Channel"
                    }
                }
                line.startsWith("#") -> { /* skip other directives (#EXTM3U, #EXTVLCOPT, …) */ }
                else -> {
                    // A media URL line closes the current entry.
                    val name = pendingName
                    if (name != null) {
                        channels.add(Channel(name, line, pendingLogo, pendingGroup))
                    }
                    pendingName = null
                    pendingLogo = null
                    pendingGroup = null
                }
            }
        }
        return channels
    }

    private fun fetch(urlString: String): String {
        var current = urlString
        var redirects = 0
        while (true) {
            val conn = (URL(current).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 20000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "VideoPlayer/1.0")
                setRequestProperty("Accept-Encoding", "gzip")
            }
            try {
                val code = conn.responseCode
                if (code in 300..399 && redirects < 5) {
                    val location = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (location.isNullOrBlank()) throw IllegalStateException("HTTP $code with no redirect target")
                    current = URL(URL(current), location).toString()
                    redirects++
                    continue
                }
                if (code !in 200..299) throw IllegalStateException("HTTP $code")

                val stream = if (conn.contentEncoding?.contains("gzip", true) == true) {
                    GZIPInputStream(conn.inputStream)
                } else {
                    conn.inputStream
                }
                return BufferedReader(InputStreamReader(stream)).use { it.readText() }
            } finally {
                conn.disconnect()
            }
        }
    }
}
