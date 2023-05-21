package com.suraj854.videotrimmerview.adapter

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.suraj854.videotrimmerview.R


class FrameAdapter(private val videoUri: Uri) : RecyclerView.Adapter<FrameAdapter.FrameViewHolder>() {
    private val frames: ArrayList<Bitmap> = ArrayList()

    class FrameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val frameImageView: ImageView = itemView.findViewById(R.id.frameImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrameViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.frameview, parent, false)
        return FrameViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FrameViewHolder, position: Int) {
        val frame = frames[position]
        Glide.with(holder.itemView)
            .load(frame)
            .into(holder.frameImageView)
    }

    override fun getItemCount(): Int {
        return frames.size
    }

    fun extractFrames() {
        val retriever = MediaMetadataRetriever()
        Log.e("Video",videoUri.toString())
        retriever.setDataSource(videoUri.toString())

        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
        val interval = duration / 10 // Extract 10 frames

        for (i in 0..9) {
            val time = i * interval * 1000 // Time in microseconds
            val frame = retriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

            if (frame != null) {
                frames.add(frame)
            }
        }

        retriever.release()
        notifyDataSetChanged()
    }
}