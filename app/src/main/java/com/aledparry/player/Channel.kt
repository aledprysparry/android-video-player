package com.aledparry.player

/** One entry parsed from an IPTV M3U playlist. */
data class Channel(
    val name: String,
    val url: String,
    val logo: String?,
    val group: String?
)
