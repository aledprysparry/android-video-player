package com.aledparry.player

import android.net.Uri

/** One playable video discovered on the device. */
data class VideoItem(
    val id: Long,
    val uri: Uri,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long
)
