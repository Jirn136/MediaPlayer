package com.example.musicplayer.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.databinding.ItemViewVideoListBinding
import com.example.musicplayer.model.VideoDetails

class VideoListAdapter(
    val clickListener: (details: VideoDetails,position:Int) -> Unit,
) : ListAdapter<VideoDetails, VideoListAdapter.VideoListViewHolder>(DiffUtilCallBack()) {
    inner class VideoListViewHolder(private val binding: ItemViewVideoListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                getItem(position).apply {
                    txtTitle.text = title
                    image?.let {
                        imgThumbnail.setImageBitmap(Bitmap.createBitmap(it))
                    }
                    binding.root.setOnClickListener {
                        clickListener(this,position)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoListViewHolder =
        VideoListViewHolder(
            ItemViewVideoListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: VideoListViewHolder, position: Int) {
        holder.bind(position)
    }

}

private class DiffUtilCallBack : DiffUtil.ItemCallback<VideoDetails>() {
    override fun areItemsTheSame(oldItem: VideoDetails, newItem: VideoDetails): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: VideoDetails, newItem: VideoDetails): Boolean {
        return oldItem == newItem
    }

}