package com.aledparry.player

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aledparry.player.databinding.ActivityChannelListBinding
import com.google.android.material.chip.Chip

class ChannelListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChannelListBinding
    private lateinit var adapter: ChannelAdapter

    private var allChannels: List<Channel> = emptyList()
    private var query: String = ""
    private var selectedGroup: String? = null   // null = All

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        allChannels = PlaylistHolder.channels
        binding.toolbar.title = PlaylistHolder.sourceTitle
        binding.toolbar.subtitle = getString(R.string.ch_count, allChannels.size)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = ChannelAdapter { channel ->
            startActivity(
                Intent(this, PlayerActivity::class.java)
                    .putExtra(PlayerActivity.EXTRA_URI, channel.url)
                    .putExtra(PlayerActivity.EXTRA_TITLE, channel.name)
            )
        }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        buildGroupChips()

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                query = s?.toString()?.trim().orEmpty()
                applyFilter()
            }
        })

        applyFilter()
    }

    private fun buildGroupChips() {
        val groups = allChannels.mapNotNull { it.group?.takeIf { g -> g.isNotBlank() } }
            .distinct()
            .sorted()

        // "All" chip
        addChip(getString(R.string.ch_all_groups), null, checked = true)
        groups.forEach { addChip(it, it, checked = false) }

        binding.groupChips.setOnCheckedStateChangeListener { group, checkedIds ->
            val id = checkedIds.firstOrNull()
            selectedGroup = if (id == null) null else group.findViewById<Chip>(id)?.tag as? String
            applyFilter()
        }
    }

    private fun addChip(label: String, tag: String?, checked: Boolean) {
        val chip = layoutInflater.inflate(R.layout.item_chip, binding.groupChips, false) as Chip
        chip.text = label
        chip.tag = tag
        chip.isChecked = checked
        binding.groupChips.addView(chip)
    }

    private fun applyFilter() {
        val q = query.lowercase()
        val filtered = allChannels.filter { ch ->
            val matchesGroup = selectedGroup == null || ch.group == selectedGroup
            val matchesQuery = q.isEmpty() || ch.name.lowercase().contains(q)
            matchesGroup && matchesQuery
        }
        adapter.submitList(filtered)
        binding.emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
