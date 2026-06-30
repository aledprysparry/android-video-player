package com.aledparry.player

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aledparry.player.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: VideoAdapter

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) loadVideos() else showPermissionDenied()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        adapter = VideoAdapter { video ->
            startActivity(
                Intent(this, PlayerActivity::class.java)
                    .putExtra(PlayerActivity.EXTRA_URI, video.uri.toString())
                    .putExtra(PlayerActivity.EXTRA_TITLE, video.title)
            )
        }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.grantButton.setOnClickListener { ensurePermission() }
        binding.swipeRefresh.setOnRefreshListener { ensurePermission() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_network_stream -> {
                startActivity(Intent(this, NetworkStreamActivity::class.java)); true
            }
            R.id.action_help -> {
                startActivity(Intent(this, HelpActivity::class.java)); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        ensurePermission()
    }

    private fun mediaPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_VIDEO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

    private fun ensurePermission() {
        val perm = mediaPermission()
        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) {
            loadVideos()
        } else {
            requestPermission.launch(perm)
        }
    }

    private fun loadVideos() {
        binding.permissionState.visibility = android.view.View.GONE
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            val videos = withContext(Dispatchers.IO) {
                VideoRepository.queryVideos(this@MainActivity)
            }
            binding.swipeRefresh.isRefreshing = false
            adapter.submitList(videos) { binding.recycler.scheduleLayoutAnimation() }
            binding.emptyState.visibility =
                if (videos.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun showPermissionDenied() {
        binding.swipeRefresh.isRefreshing = false
        binding.emptyState.visibility = android.view.View.GONE
        binding.permissionState.visibility = android.view.View.VISIBLE
    }
}
