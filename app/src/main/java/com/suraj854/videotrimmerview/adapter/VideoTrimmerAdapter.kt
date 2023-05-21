package com.suraj854.videotrimmerview.adapter

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.suraj854.videotrimmerview.R

class VideoTrimmerAdapter(private val context: Context) :
    RecyclerView.Adapter<VideoTrimmerAdapter.TrimmerViewHolder>() {
    private val mBitmaps: MutableList<Bitmap> = ArrayList()
    private val mInflater: LayoutInflater

    init {
        mInflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrimmerViewHolder {
        return TrimmerViewHolder(mInflater.inflate(R.layout.video_thumb_item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: TrimmerViewHolder, position: Int) {
        holder.thumbImageView.setImageBitmap(mBitmaps[position])
    }


    override fun getItemCount(): Int {
        return mBitmaps.size
    }

    fun addBitmaps(bitmap: Bitmap) {
        mBitmaps.add(bitmap)
        Log.e("bitmao", "Added")
        notifyDataSetChanged()
    }

    class TrimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbImageView: ImageView = itemView.findViewById(R.id.thumb)
    }


}