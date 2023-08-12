package com.example.musicplayer.player

import android.app.PictureInPictureParams
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource2
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.musicplayer.R
import com.example.musicplayer.activities.ControllerActivity
import com.example.musicplayer.databinding.CustomControlsBinding
import com.example.musicplayer.databinding.FragmentPlayerBinding
import com.example.musicplayer.init.BaseFragment
import com.example.musicplayer.utils.Constants
import com.example.musicplayer.utils.gone
import com.example.musicplayer.utils.goneViews
import com.example.musicplayer.utils.hide
import com.example.musicplayer.utils.prettyPrint
import com.example.musicplayer.utils.show
import com.example.musicplayer.utils.showViews
import com.example.musicplayer.utils.toToast
import java.io.File

class PlayerFragment : BaseFragment<FragmentPlayerBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentPlayerBinding = FragmentPlayerBinding.inflate(inflater)

    private lateinit var customControlsBinding: CustomControlsBinding
    private lateinit var exoplayer: ExoPlayer
    private lateinit var concatenatingMediaSource: ConcatenatingMediaSource2
    private lateinit var mediaSource: MediaSource

    private lateinit var uri: Uri
    private var title: String? = null

    private var playbackSpeed = 1F
    private var position = 0
    private var isLink = false

    private val mActivity by lazy { activity as ControllerActivity }
    private val onBackPressedDispatcher by lazy { mActivity.onBackPressedDispatcher }
    private val playerArgs: PlayerFragmentArgs by navArgs()
    private var isInLandscape = false
    private var haveEnteredPip = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customControlsBinding = CustomControlsBinding.bind(binding.root)
        playerArgs.apply {
            uri = Uri.parse(StringUri)
            position = StringPosition
            title = StringTitle
            isLink = StringIslink
        }
        setupOnBackPressed()
        handleControls()
    }

    override fun onStart() {
        super.onStart()
        if (!isLink) prepareExoplayer()
        else prepareExoPlayerForLink()
    }

    override fun onResume() {
        super.onResume()
        exoplayer.apply {
            playWhenReady = true
            playbackState
        }

    }

    override fun onPause() {
        super.onPause()
        val isInPIP = mActivity.isInPictureInPictureMode
        "isInPIP: $isInPIP".prettyPrint()
        if (!isInPIP && exoplayer.isPlaying)
            exoplayer.apply {
                playWhenReady = false
                playbackState
            }
    }

    override fun onDestroy() {
        if (haveEnteredPip) mActivity.recreateActivity()
        super.onDestroy()
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun handleControls() {
        customControlsBinding.apply {
            if (isLink) goneViews(ivPrevious, ivNext, ivLock, ivUnlock, ivScaling)
            txtTitle.text = title
            if (ControllerActivity.videoList.size <= 1) {
                ivPrevious.isEnabled = false
                ivNext.isEnabled = false
            }
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
                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> {
                            ivScaling.setImageResource(R.drawable.ic_height)
                            AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                        }

                        AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT -> {
                            ivScaling.setImageResource(R.drawable.ic_fill)
                            AspectRatioFrameLayout.RESIZE_MODE_FILL
                        }

                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> {
                            ivScaling.setImageResource(R.drawable.ic_zoom)
                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        }

                        else -> {
                            ivScaling.setImageResource(R.drawable.ic_scaling)
                            AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    }
                }
            }
            ivUnlock.setOnClickListener {
                rootLayout.hide()
                ivLock.show()
            }
            ivLock.setOnClickListener {
                rootLayout.show()
                ivLock.gone()
            }
            ivPip.setOnClickListener {
                enterPIPMode()
            }
            ivNext.setOnClickListener {
                playNext()
            }
            ivPrevious.setOnClickListener {
                playPrevious()
            }

            ivPlaybackSpeed.setOnClickListener {
                updatePlaybackSpeed()
            }

            ivRotation.setOnClickListener {
                rotateScreen()
            }
        }
    }

    private fun rotateScreen() {
        mActivity.apply {
            isInLandscape = if (isInLandscape) {
                setScreenPortrait()
                false
            } else {
                setScreenLandscape()
                true
            }
        }
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(enabled = true) {
            override fun handleOnBackPressed() {
                exoplayer.stop()
                exoplayer.release()

                onBackPressedDispatcher.addCallback(this)
                finish()
            }
        })

    }

    private fun updatePlaybackSpeed() {
        customControlsBinding.apply {
            playbackSpeed = when (playbackSpeed) {
                1F -> {
                    ivPlaybackSpeed.setImageResource(R.drawable.ic_2x)
                    2F
                }

                2F -> {
                    ivPlaybackSpeed.setImageResource(R.drawable.ic_3x)
                    3F
                }

                3F -> {
                    ivPlaybackSpeed.setImageResource(R.drawable.ic_4x)
                    4F
                }

                else -> {
                    ivPlaybackSpeed.setImageResource(R.drawable.ic_1x)
                    1F
                }
            }
            exoplayer.setPlaybackSpeed(playbackSpeed)
        }
    }

    private fun playPrevious() {
        customControlsBinding.apply {
            try {
                position--
                if (position == 0) ivPrevious.isEnabled = false
                if (!ivNext.isEnabled) ivNext.isEnabled = true
                if (position >= 0) {
                    exoplayer.stop()
                    prepareExoplayer()
                } else {
                    ivPrevious.isEnabled = false
                    "prev position $position vSize ${ControllerActivity.videoList.size}".prettyPrint()
                }
            } catch (e: Exception) {
                finish()
                e.printStackTrace()
            }
        }
    }

    private fun finish() {
        mActivity.setScreenPortrait()
        findNavController().popBackStack()
    }

    private fun retrieveWidthAndHeightAttributes(): Pair<Int, Int> {
        return if (!isLink) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(requireContext(), uri)
            val videoWidth =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
            val videoHeight =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
            "retrieveWidthAndHeightAttributes: width $videoWidth height $videoHeight".prettyPrint()
            Pair(videoWidth ?: 16, videoHeight ?: 9)
        } else Pair(16, 9)
    }

    private fun enterPIPMode() {
        val visibleRect = Rect()
        binding.exoplayerView.getGlobalVisibleRect(visibleRect)
        mActivity.enterPictureInPictureMode(
            PictureInPictureParams.Builder().apply {
                if (aspectRatioInFloat() > 1) setAspectRatio(Rational(16, 9))
                else setAspectRatio(Rational(9, 16))
                setSourceRectHint(visibleRect)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) setAutoEnterEnabled(true)
            }.build()
        )
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        haveEnteredPip = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            binding.exoplayerView.hideController()
            customControlsBinding.apply {
                goneViews(ivPlaybackSpeed, ivRotation)
            }
            exoplayer.apply {
                if (isPlaying) {
                    playWhenReady = true
                    playbackState
                }
            }
        } else {
            if (exoplayer.isPlaying) {
                binding.exoplayerView.showController()
                customControlsBinding.apply {
                    showViews(ivPlaybackSpeed, ivRotation)
                }
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun prepareExoplayer() {
        if (position == 0) customControlsBinding.ivPrevious.isEnabled = false
        else if (position == ControllerActivity.videoList.size - 1) customControlsBinding.ivNext.isEnabled =
            false

        binding.apply {
            val uri = Uri.parse(ControllerActivity.videoList[position].path)
            this@PlayerFragment.uri = uri

            exoplayer =
                ExoPlayer.Builder(requireContext()).setSeekBackIncrementMs(Constants.SKIP_DURATION)
                    .setSeekForwardIncrementMs(Constants.SKIP_DURATION).build()
            exoplayer.clearMediaItems()

            repeat(ControllerActivity.videoList.size) {
                File(ControllerActivity.videoList[it].toString())
                mediaSource =
                    ProgressiveMediaSource.Factory(DefaultDataSource.Factory(requireContext()))
                        .createMediaSource(MediaItem.fromUri(uri))
            }

            concatenatingMediaSource =
                ConcatenatingMediaSource2.Builder().useDefaultMediaSourceFactory(requireContext())
                    .add(mediaSource, 0).add(MediaItem.fromUri(uri.toString()), 0).build()
            exoplayerView.apply {
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                player = exoplayer
                keepScreenOn = true
            }
            exoplayer.apply {
                setMediaSource(concatenatingMediaSource)
                setPlaybackSpeed(playbackSpeed)
                prepare()
                seekTo(0, C.TIME_UNSET)
                videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
                playWhenReady = true
            }

            playerListeners()
        }
    }

    private fun playNext() {
        customControlsBinding.apply {
            try {
                if (isLink) finish()
                position++
                if (!ivPrevious.isEnabled) ivPrevious.isEnabled = true
                if (position < ControllerActivity.videoList.size) {
                    exoplayer.stop()
                    prepareExoplayer()
                } else {
                    ivNext.isEnabled = false
                    "next position $position vSize ${ControllerActivity.videoList.size}".prettyPrint()
                    if (position == ControllerActivity.videoList.size) finish()
                }
            } catch (e: Exception) {
                finish()
                e.printStackTrace()
            }
        }
    }

    private fun playerListeners() {
        exoplayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                error.printStackTrace()
                exoplayer.stop()
                exoplayer.release()
                finish()
                "Unable to play video".toToast(requireContext())
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_READY -> {
                        "onPlayWhenReadyChanged1: READY playWhenReady ".prettyPrint()
                        "aspectRatio:: ${aspectRatioInFloat()}".prettyPrint()
                        isInLandscape = if (aspectRatioInFloat() > 1) {
                            mActivity.setScreenLandscape()
                            true
                        } else {
                            mActivity.setScreenPortrait()
                            false
                        }
                        exoplayer.play()
                    }

                    Player.STATE_BUFFERING -> "onPlayWhenReadyChanged1: BUFFERING".prettyPrint()
                    Player.STATE_IDLE -> "onPlayWhenReadyChanged1: IDLE".prettyPrint()
                    Player.STATE_ENDED -> {
                        customControlsBinding.ivPlay.setImageResource(R.drawable.ic_play)
                        "position: $position videoSize: ${ControllerActivity.videoList.size}".prettyPrint()
                        playNext()
                    }

                    else -> "onPlayWhenReadyChanged1: $playbackState".prettyPrint()


                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                "onIsPlayingChanged: isPlaying $isPlaying".prettyPrint()
                if (isPlaying) customControlsBinding.ivPlay.setImageResource(
                    R.drawable.ic_pause
                )
            }
        })
    }

    private fun aspectRatioInFloat(): Float =
        retrieveWidthAndHeightAttributes().first.toFloat() / retrieveWidthAndHeightAttributes().second


    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun prepareExoPlayerForLink() {
        exoplayer = ExoPlayer.Builder(requireContext()).build()
        binding.exoplayerView.player = exoplayer
        val mediaItem = MediaItem.fromUri(uri)
        exoplayer.apply {
            addMediaItem(mediaItem)
            seekTo(0, C.TIME_UNSET)
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            prepare()
            playWhenReady = true
        }
        playerListeners()
    }

}