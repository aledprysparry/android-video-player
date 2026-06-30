package com.aledparry.player

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.aledparry.player.databinding.ActivityPlayerBinding

@UnstableApi
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep the screen on during playback.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setupImmersiveToggle()

        savedInstanceState?.let {
            playWhenReady = it.getBoolean(STATE_PLAY_WHEN_READY, true)
            currentItem = it.getInt(STATE_ITEM, 0)
            playbackPosition = it.getLong(STATE_POSITION, 0L)
        }
    }

    private fun sourceUri(): Uri? {
        // Either launched internally with EXTRA_URI, or via an external "Open with" VIEW intent.
        intent.getStringExtra(EXTRA_URI)?.let { return Uri.parse(it) }
        return intent.data
    }

    private fun initializePlayer() {
        val uri = sourceUri()
        if (uri == null) {
            finish()
            return
        }

        val exo = ExoPlayer.Builder(this).build()
        binding.playerView.player = exo

        exo.setMediaItem(MediaItem.fromUri(uri))
        exo.playWhenReady = playWhenReady
        exo.seekTo(currentItem, playbackPosition)
        exo.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                binding.buffering.visibility =
                    if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
            }
        })
        exo.prepare()
        player = exo
    }

    private fun releasePlayer() {
        player?.let {
            playWhenReady = it.playWhenReady
            currentItem = it.currentMediaItemIndex
            playbackPosition = it.currentPosition
            it.release()
        }
        player = null
    }

    private fun setupImmersiveToggle() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, binding.root)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide system bars; the Media3 controls overlay handles interaction.
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    // Lifecycle: follow Media3 guidance — init in onStart/onResume, release in onPause/onStop.

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT > 23) initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT <= 23 || player == null) initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > 23) releasePlayer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        player?.let {
            outState.putBoolean(STATE_PLAY_WHEN_READY, it.playWhenReady)
            outState.putInt(STATE_ITEM, it.currentMediaItemIndex)
            outState.putLong(STATE_POSITION, it.currentPosition)
        }
    }

    companion object {
        const val EXTRA_URI = "extra_uri"
        const val EXTRA_TITLE = "extra_title"

        private const val STATE_PLAY_WHEN_READY = "play_when_ready"
        private const val STATE_ITEM = "item"
        private const val STATE_POSITION = "position"
    }
}
