package com.suraj854.videotrimmerview

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
import com.suraj854.videotrimmerview.utilis.VideoTrimmerUtil.VideoTrimmerUtil.RECYCLER_VIEW_PADDING
import com.suraj854.videotrimmerview.utilis.VideoTrimmerUtil.VideoTrimmerUtil.THUMB_WIDTH
import com.suraj854.videotrimmerview.utilis.VideoTrimmerUtil.VideoTrimmerUtil.VIDEO_FRAMES_WIDTH
import com.suraj854.videotrimmerview.widget.RangeSeekBarView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private val endPosition = 0
    private val totalThumbsCount = 10
    private val startPosition = 0

    lateinit var videoUri: Uri
    val addMediaChooserResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK && result.data != null) {

                val data = result.data!!
                mSourceUri = Uri.parse(data.dataString)

                player.setVideoURI(mSourceUri)
                player.requestFocus()
                player.setOnPreparedListener {
                    it.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                    videoPrepared(it)
                }


            }

        }
    private var mSourceUri: Uri? = null
    private var mLinearVideo: RelativeLayout? = null
    private fun videoPrepared(mp: MediaPlayer) {
        val lp: ViewGroup.LayoutParams = player.getLayoutParams()
        val videoWidth = mp.videoWidth
        val videoHeight = mp.videoHeight

        val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
        val screenWidth: Int? = mLinearVideo?.getWidth()
        val screenHeight: Int? = mLinearVideo?.getHeight()

        if (videoHeight > videoWidth) {
            if (screenWidth != null) {
                lp.width = screenWidth
            }
            if (screenHeight != null) {
                lp.height = screenHeight
            }
        } else {
            if (screenWidth != null) {
                lp.width = screenWidth
            }
            val r = videoHeight / videoWidth.toFloat()
            lp.height = (lp.width * r).toInt()
        }
        player.setLayoutParams(lp)
        mDuration = player.getDuration()
        /*  if (!getRestoreState()) {
              seekTo(mRedProgressBarPos.toInt().toLong())
          } else {
              setRestoreState(false)
              seekTo(mRedProgressBarPos.toInt().toLong())
          }*/

        initRangeSeekBarView()
        CoroutineScope(Dispatchers.Main).launch {

            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(
                this@MainActivity,
                mSourceUri
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

    private var isOverScaledTouchSlop = false
    private val mScaledTouchSlop = 0
    private val mOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Log.d(TAG, "newState = $newState")
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isSeeking = false
                val scrollX: Int = calcScrollXDistance()

                if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
                    isOverScaledTouchSlop = false
                    return
                }
                isOverScaledTouchSlop = true
                //初始状态,why ? 因为默认的时候有35dp的空白！
                if (scrollX == -RECYCLER_VIEW_PADDING) {
                    scrollPos = 0
                    mLeftProgressPos = mRangeSeekBarView.selectedMinValue + scrollPos
                    mRightProgressPos = mRangeSeekBarView.selectedMaxValue + scrollPos
                    Log.d(
                        TAG,
                        "onScrolled >>>> mLeftProgressPos = $mLeftProgressPos"
                    )
                    mRedProgressBarPos = mLeftProgressPos
                } else {
                    isSeeking = true
                    scrollPos =
                        ((mAverageMsPx * (RECYCLER_VIEW_PADDING + scrollX) / THUMB_WIDTH).toLong())
                    mLeftProgressPos = mRangeSeekBarView.selectedMinValue + scrollPos
                    mRightProgressPos = mRangeSeekBarView.selectedMaxValue + scrollPos
                    Log.d(
                        TAG,
                        "onScrolled >>>> mLeftProgressPos = $mLeftProgressPos"
                    )
                    mRedProgressBarPos = mLeftProgressPos
                    if (player.isPlaying()) {
                        player.pause()
                        setPlayPauseViewIcon(false)
                    }
                    mRedProgressIcon.setVisibility(View.GONE)
                    seekTo(mLeftProgressPos)
                    mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)
                    mRangeSeekBarView.invalidate()
                }
                lastScrollX = scrollX
            }
        }
    private var lastScrollX = 0
    private fun playVideoOrPause() {

        mRedProgressBarPos = player.currentPosition.toLong()
        if (player.isPlaying()) {
            player.pause()
            pauseRedProgressAnimation()
        } else {
            player.start()
            playingRedProgressAnimation()
        }
        setPlayPauseViewIcon(player.isPlaying())
    }

    private var mPlayView: ImageView? = null
    private fun setPlayPauseViewIcon(isPlaying: Boolean) {
        mPlayView?.setImageResource(if (isPlaying) R.drawable.ic_video_pause_black else R.drawable.ic_video_play_black)

    }

    private fun seekTo(msec: Long) {
        player.seekTo(msec.toInt())
        Log.d(TAG, "seekTo = $msec")
    }

    fun onVideoPause() {
        if (player.isPlaying()) {
            seekTo(mLeftProgressPos) //复位
            player.pause()
            setPlayPauseViewIcon(false)
            mRedProgressIcon.visibility = View.GONE
        }
    }

    private fun updateVideoProgress() {
        val currentPosition: Long = player.currentPosition.toLong()
        Log.d(
            TAG,
            "updateVideoProgress currentPosition = $currentPosition"
        )
        if (currentPosition >= mRightProgressPos) {
            mRedProgressBarPos = mLeftProgressPos
            pauseRedProgressAnimation()
            onVideoPause()
        } else {
            mAnimationHandler.post(mAnimationRunnable)
        }
    }

    private val mAnimationRunnable = Runnable { updateVideoProgress() }
    private fun pauseRedProgressAnimation() {
        mRedProgressIcon.clearAnimation()
        if (mRedProgressAnimator != null && mRedProgressAnimator!!.isRunning) {
            mAnimationHandler.removeCallbacks(mAnimationRunnable)
            mRedProgressAnimator!!.cancel()
        }
    }

    private fun playingRedProgressAnimation() {
        pauseRedProgressAnimation()
        playingAnimation()
        mAnimationHandler.post(mAnimationRunnable)
    }

    private var mRedProgressAnimator: ValueAnimator? = null
    private val mAnimationHandler = Handler()
    private fun playingAnimation() {
        if (mRedProgressIcon.visibility == View.GONE) {
            mRedProgressIcon.visibility = View.VISIBLE
        }
        val params = mRedProgressIcon.layoutParams as FrameLayout.LayoutParams
        val start = (RECYCLER_VIEW_PADDING + (mRedProgressBarPos - scrollPos) * averagePxMs).toInt()
        val end = (RECYCLER_VIEW_PADDING + (mRightProgressPos - scrollPos) * averagePxMs).toInt()
        mRedProgressAnimator = ValueAnimator.ofInt(start, end)
            .setDuration(mRightProgressPos - scrollPos - (mRedProgressBarPos - scrollPos))
        mRedProgressAnimator?.interpolator = LinearInterpolator()
        mRedProgressAnimator?.addUpdateListener(AnimatorUpdateListener { animation ->
            params.leftMargin = animation.animatedValue as Int
            mRedProgressIcon.layoutParams = params
            Log.d(
                TAG,
                "----onAnimationUpdate--->>>>>>>$mRedProgressBarPos"
            )
        })
        mRedProgressAnimator?.start()
    }

    private fun calcScrollXDistance(): Int {
        val layoutManager = video_frames_recyclerView.getLayoutManager() as LinearLayoutManager
        val position = layoutManager.findFirstVisibleItemPosition()
        val firstVisibleChildView = layoutManager.findViewByPosition(position)
        val itemWidth = firstVisibleChildView!!.width
        return position * itemWidth - firstVisibleChildView.left
    }

    private var isSeeking = false
    lateinit var player: VideoView
    lateinit var button: Button
    lateinit var video_frames_recyclerView: RecyclerView
    lateinit var frameAdapter: VideoTrimmerAdapter
    lateinit var seekBarLayout: LinearLayout
    lateinit var mRangeSeekBarView: RangeSeekBarView
    private var mLeftProgressPos: Long = 0
    private var mRightProgressPos: Long = 0
    private var mRedProgressBarPos: Long = 0
    private var mMaxWidth: Int = 0
    private var scrollPos: Long = 0
    lateinit var mRedProgressIcon: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MediaTypeUtils.initialize(this)
        player = findViewById(R.id.playerView)
        mPlayView = findViewById(R.id.icon_video_play)
        button = findViewById(R.id.search_bar)
        mLinearVideo = findViewById(R.id.layout_surface_view)
        VideoTrimmerUtil.initialize(this)
        seekBarLayout = findViewById(R.id.seekBarLayout)
        mRedProgressIcon = findViewById<ImageView>(R.id.positionIcon)
        mMaxWidth = VIDEO_FRAMES_WIDTH
        video_frames_recyclerView = findViewById(R.id.video_frames_recyclerView)
        video_frames_recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        frameAdapter = VideoTrimmerAdapter(this)
        mPlayView?.setOnClickListener({ playVideoOrPause() })

        video_frames_recyclerView.adapter = frameAdapter
        video_frames_recyclerView.addOnScrollListener(mOnScrollListener)
        button.setOnClickListener {
            if (checkCamStoragePer(this)) {
                openMultipleMedia()
            }
        }

    }

    private var mAverageMsPx //每毫秒所占的px
            = 0f
    private var averagePxMs = 0f
    private var mDuration: Int = 0
    private var mThumbsTotalCount = 0
    private fun initRangeSeekBarView() {

        mLeftProgressPos = 0
        if (mDuration <= MAX_SHOOT_DURATION) {
            mThumbsTotalCount = MAX_COUNT_RANGE
            mRightProgressPos = mDuration.toLong()
        } else {
            mThumbsTotalCount =
                (mDuration * 1.0f / (MAX_SHOOT_DURATION * 1) * MAX_COUNT_RANGE).roundToInt()
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
                mLeftProgressPos = minValue + scrollPos
                mRedProgressBarPos = mLeftProgressPos
                mRightProgressPos = maxValue + scrollPos

                when (action) {
                    MotionEvent.ACTION_DOWN -> isSeeking = false
                    MotionEvent.ACTION_MOVE -> {
                        isSeeking = true
                        seekTo(
                            (if (pressedThumb === RangeSeekBarView.Thumb.MIN) mLeftProgressPos else mRightProgressPos).toInt()
                                .toLong()
                        )
                    }

                    MotionEvent.ACTION_UP -> {
                        isSeeking = false
                        seekTo(mLeftProgressPos.toInt().toLong())

                    }

                    else -> {

                    }
                }

                mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos)
            }

        })
        seekBarLayout.addView(mRangeSeekBarView)
        if (mThumbsTotalCount - MAX_COUNT_RANGE > 0) {
            mAverageMsPx =
                (mDuration - MAX_SHOOT_DURATION) / (mThumbsTotalCount - MAX_COUNT_RANGE).toFloat()
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



