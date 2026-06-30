package com.aledparry.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aledparry.player.databinding.ItemVideoBinding
import com.bumptech.glide.Glide

class VideoAdapter(
    private val onClick: (VideoItem) -> Unit
) : ListAdapter<VideoItem, VideoAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemVideoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            title.text = item.title
            meta.text = listOf(Format.duration(item.durationMs), Format.size(item.sizeBytes))
                .filter { it.isNotEmpty() }
                .joinToString("  •  ")

            Glide.with(thumbnail)
                .load(item.uri)
                .centerCrop()
                .placeholder(R.drawable.bg_thumb_placeholder)
                .error(R.drawable.bg_thumb_placeholder)
                .into(thumbnail)

            root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<VideoItem>() {
            override fun areItemsTheSame(a: VideoItem, b: VideoItem) = a.id == b.id
            override fun areContentsTheSame(a: VideoItem, b: VideoItem) = a == b
        }
    }
}
