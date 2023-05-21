package com.suraj854.videotrimmerview.utilis

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.TextUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Author：J.Chou
 * Date：  2016.08.01 2:23 PM
 * Email： who_know_me@163.com
 * Describe:
 */
/*
class VideoTrimmerUtil2() {


    companion object VideoTrimmerUtil {



        private val TAG = VideoTrimmerUtil2::class.java.simpleName
        const val MIN_SHOOT_DURATION = 3000L // 最小剪辑时间3s
        const val VIDEO_MAX_TIME = 10 // 10秒
        const val MAX_SHOOT_DURATION = VIDEO_MAX_TIME * 1000L //视频最多剪切多长时间10s
        const val MAX_COUNT_RANGE = 10 //seekBar的区域内一共有多少张图片
        private var SCREEN_WIDTH_FULL =0
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        var RECYCLER_VIEW_PADDING = 0
        val VIDEO_FRAMES_WIDTH = SCREEN_WIDTH_FULL - RECYCLER_VIEW_PADDING * 2
        val THUMB_WIDTH = (SCREEN_WIDTH_FULL - RECYCLER_VIEW_PADDING * 2) / VIDEO_MAX_TIME

        val VIDEO_FRAMES_WIDTH=0
        val THUMB_HEIGHT = UnitConverter().dpToPx(50)
        fun getVideoFilePath(url: String): String {
            var url = url
            if (TextUtils.isEmpty(url) || url.length < 5) return ""
            if (url.substring(0, 4).equals("http", ignoreCase = true)) {
            } else {
                url = "file://$url"
            }
            return url
        }
        fun initialize(context: Context) {
            this.context = context.applicationContext
            DeviceUtil.initialize(this.context)
            BaseUtils.init(this.context)
            SCREEN_WIDTH_FULL   = DeviceUtil.deviceWidth
            val VIDEO_FRAMES_WIDTH = SCREEN_WIDTH_FULL - RECYCLER_VIEW_PADDING * 2
            RECYCLER_VIEW_PADDING=  UnitConverter().dpToPx(35)
        }

        private fun convertSecondsToTime(seconds: Long): String {
            var timeStr: String? = null
            var hour = 0
            var minute = 0
            var second = 0
            if (seconds <= 0) {
                return "00:00"
            } else {
                minute = seconds.toInt() / 60
                if (minute < 60) {
                    second = seconds.toInt() % 60
                    timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second)
                } else {
                    hour = minute / 60
                    if (hour > 99) return "99:59:59"
                    minute = minute % 60
                    second = (seconds - hour * 3600 - minute * 60).toInt()
                    timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second)
                }
            }
            return timeStr
        }

        private fun unitFormat(i: Int): String {
            var retStr: String? = null
            retStr = if (i >= 0 && i < 10) {
                "0" + Integer.toString(i)
            } else {
                "" + i
            }
            return retStr
        }


    }

    fun shootVideoThumbInBackground(
        context: Context?, videoUri: Uri?, totalThumbsCount: Int, startPosition: Long,
        endPosition: Long
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(context, videoUri)
                // Retrieve media data use microsecond
                val interval = (endPosition - startPosition) / (totalThumbsCount - 1)
                for (i in 0 until totalThumbsCount) {
                    val frameTime = startPosition + interval * i
                    var bitmap: Bitmap? = mediaMetadataRetriever.getFrameAtTime(
                        frameTime * 1000,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                        ?: continue
                    try {
                        bitmap = bitmap?.let {
                            Bitmap.createScaledBitmap(
                                it,
                                THUMB_WIDTH,
                                THUMB_HEIGHT,
                                false
                            )
                        }
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }

                }
                mediaMetadataRetriever.release()
            } catch (e: Throwable) {

            }
        }

    }


}*/
