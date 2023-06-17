package com.example.musicplayer.activities

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityPlayerBinding
import com.example.musicplayer.databinding.CustomControlsBinding
import com.example.musicplayer.utils.Constants
import com.example.musicplayer.utils.setFullScreen
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ConcatenatingMediaSource2
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoSize

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var customControlsBinding: CustomControlsBinding
    private var uri: Uri? = null
    private var title: String? = null
    private lateinit var exoplayer: ExoPlayer
    private lateinit var concatenatingMediaSource: ConcatenatingMediaSource2
    private lateinit var mediaSession: MediaSessionCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        customControlsBinding = CustomControlsBinding.bind(binding.root)
        setContentView(binding.root)
        setFullScreen()

        supportActionBar?.hide()

        intent?.let {
            uri = Uri.parse(it.getStringExtra(Constants.URI))
            title = it.getStringExtra(Constants.TITLE)
        }


        prepareExoplayer()
        setupOnBackPressed()
        handleControls()
    }

    override fun onPause() {
        super.onPause()
        if (!isInPictureInPictureMode)
            exoplayer.apply {
                playWhenReady = false
                playbackState
            }
    }

    override fun onResume() {
        super.onResume()
        if (!isInPictureInPictureMode)
            exoplayer.apply {
                playWhenReady = true
                playbackState
            }
    }

    override fun onStart() {
        super.onStart()
        mediaSession = MediaSessionCompat(this, packageName)
        val mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoplayer)
        mediaSession.isActive = true
    }

    override fun onStop() {
        super.onStop()
        mediaSession.isActive = false
    }

    override fun onDestroy() {
        if (mediaSession.isActive) mediaSession.release()
        super.onDestroy()
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(enabled = true) {
            override fun handleOnBackPressed() {
                if (exoplayer.isPlaying) {
                    exoplayer.stop()
                    mediaSession.release()
                    exoplayer.release()
                }
                onBackPressedDispatcher.addCallback(this)
                finish()
            }
        })

    }

    private fun handleControls() {
        customControlsBinding.apply {
            txtTitle.text = title
            ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
            ivPlay.apply {
                setOnClickListener {
                    if (exoplayer.isPlaying) {
                        setImageResource(R.drawable.ic_play)
                        exoplayer.pause()
                    } else {
                        setImageResource(R.drawable.ic_pause)
                        exoplayer.play()
                    }
                }
            }
            ivRewind.setOnClickListener { exoplayer.seekBack() }
            ivSeekForward.setOnClickListener { exoplayer.seekForward() }
            ivScaling.setOnClickListener {
                binding.exoplayerView.apply {
                    resizeMode = when (resizeMode) {
                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                        AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                        AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                }
            }
            ivPip.setOnClickListener {
                enterPIPMode()
            }
        }
    }

    private fun enterPIPMode() {
        val aspectRatio =
            Rational(binding.exoplayerView.width, binding.exoplayerView.height)
        val visibleRect = Rect()
        binding.exoplayerView.getGlobalVisibleRect(visibleRect)
        enterPictureInPictureMode(
            PictureInPictureParams.Builder().apply {
                setAspectRatio(aspectRatio)
                setSourceRectHint(visibleRect)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setAutoEnterEnabled(true)
                }
            }.build()
        )
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) binding.exoplayerView.hideController()
        else binding.exoplayerView.showController()

    }

    private fun prepareExoplayer() {
        binding.apply {
            exoplayer = ExoPlayer.Builder(this@PlayerActivity)
                .setSeekBackIncrementMs(Constants.SKIP_DURATION)
                .setSeekForwardIncrementMs(Constants.SKIP_DURATION)
                .build()

            concatenatingMediaSource =
                ConcatenatingMediaSource2.Builder()
                    .useDefaultMediaSourceFactory(this@PlayerActivity)
                    .add(MediaItem.fromUri(uri.toString()), 0)
                    .build()
            exoplayerView.apply {
                player = exoplayer
                keepScreenOn = true
                setControllerVisibilityListener {
                    if (isInPictureInPictureMode) hideController()
                }
            }
            exoplayer.apply {
                setMediaSource(concatenatingMediaSource)
                prepare()
                seekTo(0, C.TIME_UNSET)
                videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
                play()
            }

            playerListeners()
        }
    }

    private fun playerListeners() {
        exoplayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                error.printStackTrace()
                if (exoplayer.isPlaying) {
                    exoplayer.stop()
                    mediaSession.release()
                    exoplayer.release()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_READY -> {
                        Log.i("kanaku", "onPlayWhenReadyChanged1: READY playWhenReady ")
                    }

                    Player.STATE_BUFFERING -> Log.i("kanaku", "onPlayWhenReadyChanged1: BUFFERING")
                    Player.STATE_IDLE -> Log.i("kanaku", "onPlayWhenReadyChanged1: IDLE")
                    Player.STATE_ENDED -> {
                        customControlsBinding.ivPlay.setImageResource(R.drawable.ic_play)
                        finish()
                    }

                    else -> {
                        Log.i("kanaku", "onPlayWhenReadyChanged1: $playbackState")
                    }

                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                Log.i("kanaku", "onIsPlayingChanged: isPlaying $isPlaying")
                if (!isInPictureInPictureMode && isPlaying)
                    customControlsBinding.ivPlay.setImageResource(R.drawable.ic_pause)
            }
        })
    }

}