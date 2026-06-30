package com.aledparry.player

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aledparry.player.databinding.ActivityNetworkStreamBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NetworkStreamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNetworkStreamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNetworkStreamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        // Preset cards
        binding.sportsContent.presetName.text = getString(R.string.ns_sports_name)
        binding.sportsContent.presetDesc.text = getString(R.string.ns_sports_desc)
        binding.fullTvContent.presetName.text = getString(R.string.ns_fulltv_name)
        binding.fullTvContent.presetDesc.text = getString(R.string.ns_fulltv_desc)

        binding.cardSports.setOnClickListener {
            loadFromPreset(getString(R.string.ns_sports_url), getString(R.string.ns_sports_name))
        }
        binding.cardFullTv.setOnClickListener {
            loadFromPreset(getString(R.string.ns_fulltv_url), getString(R.string.ns_fulltv_name))
        }

        binding.playButton.setOnClickListener { playDirect() }
        binding.loadButton.setOnClickListener { loadPlaylist(currentUrl(), getString(R.string.ch_search_hint)) }

        binding.urlInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadPlaylist(currentUrl(), getString(R.string.ch_search_hint)); true
            } else false
        }
    }

    private fun currentUrl(): String = binding.urlInput.text?.toString()?.trim().orEmpty()

    private fun playDirect() {
        val url = currentUrl()
        if (url.isEmpty()) {
            binding.urlLayout.error = getString(R.string.ns_empty_url)
            return
        }
        binding.urlLayout.error = null
        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra(PlayerActivity.EXTRA_URI, url)
        )
    }

    private fun loadFromPreset(url: String, title: String) {
        binding.urlInput.setText(url)
        loadPlaylist(url, title)
    }

    private fun loadPlaylist(url: String, title: String) {
        if (url.isEmpty()) {
            binding.urlLayout.error = getString(R.string.ns_empty_url)
            return
        }
        binding.urlLayout.error = null
        setLoading(true)

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { M3uParser.load(url) }
            setLoading(false)
            when (result) {
                is M3uParser.LoadResult.Playlist -> {
                    PlaylistHolder.channels = result.channels
                    PlaylistHolder.sourceTitle = title
                    startActivity(Intent(this@NetworkStreamActivity, ChannelListActivity::class.java))
                }
                is M3uParser.LoadResult.SingleStream -> {
                    // Not a channel list — it's a single live stream. Play it.
                    startActivity(
                        Intent(this@NetworkStreamActivity, PlayerActivity::class.java)
                            .putExtra(PlayerActivity.EXTRA_URI, url)
                    )
                }
                is M3uParser.LoadResult.Error -> {
                    binding.urlLayout.error = result.message
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.loadingOverlay.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.playButton.isEnabled = !loading
        binding.loadButton.isEnabled = !loading
        binding.cardSports.isEnabled = !loading
        binding.cardFullTv.isEnabled = !loading
    }
}
