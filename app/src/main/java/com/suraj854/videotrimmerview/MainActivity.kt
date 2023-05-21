package com.suraj854.videotrimmerview

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.VideoView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.suraj854.trimmodule.utilis.MediaTypeUtils
import com.suraj854.trimmodule.utilis.MediaTypeUtils.MediaUtils.checkCamStoragePer
import com.suraj854.videotrimmerview.adapter.VideoTrimmerAdapter
import com.suraj854.videotrimmerview.utilis.VideoTrimmerUtil
import com.suraj854.videotrimmerview.utilis.VideoTrimmerUtil.VideoTrimmerUtil.MAX_COUNT_RANGE
import com.suraj854.videotrimmerview.utilis.VideoTrimmerUtil.VideoTrimmerUtil.MAX_SHOOT_DURATION
import com.suraj854.videotrimmerview.utilis.VideoTrimmerUtil.VideoTrimmerUtil.VIDEO_FRAMES_WIDTH
import com.suraj854.videotrimmerview.widget.RangeSeekBarView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val endPosition = 0
    private val totalThumbsCount = 10
    private val startPosition = 0

    lateinit var videoUri: Uri
    val addMediaChooserResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK && result.data != null) {

                val data = result.data!!
                player.setVideoURI(Uri.parse(data.dataString))
                player.start()
                initRangeSeekBarView()
                CoroutineScope(Dispatchers.Main).launch {

                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(
                        this@MainActivity,
                        (Uri.parse(data.dataString))
                    )// Retrieve media data use microsecond
                    val interval = (endPosition - 0) / (totalThumbsCount - 1)
                    for (i in 0 until totalThumbsCount) {
                        val frameTime = startPosition + interval * i
                        var bitmap: Bitmap? = mediaMetadataRetriever.getFrameAtTime(
                            (frameTime * 1000).toLong(),
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                        )
                            ?: continue

                        bitmap = bitmap?.let {
                            Bitmap.createScaledBitmap(
                                it,
                                VideoTrimmerUtil.THUMB_WIDTH,
                                VideoTrimmerUtil.THUMB_HEIGHT,
                                false
                            )
                        }
                        if (bitmap != null) {
                            frameAdapter.addBitmaps(bitmap)
                        }


                    }
                    mediaMetadataRetriever.release()

                }


            }

        }
    lateinit var player: VideoView
    lateinit var button: Button
    lateinit var video_frames_recyclerView: RecyclerView
    lateinit var frameAdapter: VideoTrimmerAdapter
    lateinit var seekBarLayout: LinearLayout
    lateinit var mRangeSeekBarView: RangeSeekBarView
    private var mLeftProgressPos: Long = 0
    private var mRightProgressPos: Long = 0
    private val mRedProgressBarPos: Long = 0
    private var mMaxWidth: Int = 0
    private val scrollPos: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MediaTypeUtils.initialize(this)
        player = findViewById(R.id.playerView)
        button = findViewById(R.id.search_bar)
        VideoTrimmerUtil.initialize(this)
        seekBarLayout = findViewById(R.id.seekBarLayout)

        mMaxWidth = VIDEO_FRAMES_WIDTH
        video_frames_recyclerView = findViewById(R.id.video_frames_recyclerView)
        video_frames_recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        frameAdapter = VideoTrimmerAdapter(this)
        video_frames_recyclerView.adapter = frameAdapter


        button.setOnClickListener {
            if (checkCamStoragePer(this)) {
                openMultipleMedia()
            }
        }

    }

    private var mAverageMsPx //每毫秒所占的px
            = 0f
    private var averagePxMs //每px所占用的ms毫秒
            = 0f
    private val mDuration = 0
    private var mThumbsTotalCount = 0
    private fun initRangeSeekBarView() {

        mLeftProgressPos = 0
        if (mDuration <= MAX_SHOOT_DURATION) {
            mThumbsTotalCount = MAX_COUNT_RANGE
            mRightProgressPos = mDuration.toLong()
        } else {
            mThumbsTotalCount =
                (mDuration * 1.0f / (MAX_SHOOT_DURATION * 1.0f) * MAX_COUNT_RANGE) as Int
            mRightProgressPos = MAX_SHOOT_DURATION
        }
        mRangeSeekBarView = RangeSeekBarView(applicationContext, mLeftProgressPos, mRightProgressPos)
        mRangeSeekBarView.selectedMaxValue = mLeftProgressPos
        mRangeSeekBarView.selectedMaxValue = mRightProgressPos
        mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)
        mRangeSeekBarView.setMinShootTime(VideoTrimmerUtil.MIN_SHOOT_DURATION)
        mRangeSeekBarView.isNotifyWhileDragging = true
        mRangeSeekBarView.setOnRangeSeekBarChangeListener(object :
            RangeSeekBarView.OnRangeSeekBarChangeListener {
            override fun onRangeSeekBarValuesChanged(
                bar: RangeSeekBarView?,
                minValue: Long,
                maxValue: Long,
                action: Int,
                isMin: Boolean,
                pressedThumb: RangeSeekBarView.Thumb?
            ) {

            }

        })
        seekBarLayout.addView(mRangeSeekBarView)
        if (mThumbsTotalCount - MAX_COUNT_RANGE > 0) {
            mAverageMsPx =
                (mDuration - MAX_SHOOT_DURATION) / (mThumbsTotalCount - MAX_COUNT_RANGE) as Float
        } else {
            mAverageMsPx = 0f
        }
        averagePxMs = mMaxWidth * 1.0f / (mRightProgressPos - mLeftProgressPos)
    }

    private fun openMultipleMedia() {
        try {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            addMediaChooserResult.launch(Intent.createChooser(intent, "Select Video"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}



