package com.example.musicplayer.adapter

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.databinding.ItemViewVideoListBinding
import com.example.musicplayer.model.VideoDetails

class VideoListAdapter(
    private val videoDetails: ArrayList<VideoDetails>,
    val clickListener: (details:VideoDetails) -> Unit,
) :
    RecyclerView.Adapter<VideoListAdapter.VideoListViewHolder>() {
    inner class VideoListViewHolder(private val binding: ItemViewVideoListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                videoDetails[position].apply {
                    txtTitle.text = title
                    image?.let {
                        imgThumbnail.setImageBitmap(Bitmap.createBitmap(it))
                    }
                    binding.root.setOnClickListener {
                        clickListener(this)
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


    override fun getItemCount(): Int = videoDetails.size

    override fun onBindViewHolder(holder: VideoListViewHolder, position: Int) {
        holder.bind(position)
    }
}