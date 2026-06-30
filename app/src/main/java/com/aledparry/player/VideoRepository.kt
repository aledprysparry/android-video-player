package com.aledparry.player

import android.content.Context
import android.content.ContentUris
import android.provider.MediaStore

/** Reads the device's video library via MediaStore (no raw file paths needed). */
object VideoRepository {

    fun queryVideos(context: Context): List<VideoItem> {
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        val results = ArrayList<VideoItem>()

        context.contentResolver.query(
            collection, projection, null, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                results.add(
                    VideoItem(
                        id = id,
                        uri = uri,
                        title = cursor.getString(nameCol) ?: "Untitled",
                        durationMs = cursor.getLong(durCol),
                        sizeBytes = cursor.getLong(sizeCol)
                    )
                )
            }
        }
        return results
    }
}
