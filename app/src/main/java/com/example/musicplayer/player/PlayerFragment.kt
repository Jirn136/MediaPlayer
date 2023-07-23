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
import androidx.media3.session.MediaSession
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView.ControllerVisibilityListener
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
    private lateinit var mediaSession: MediaSession
    private lateinit var mediaSource: MediaSource

    private lateinit var uri: Uri
    private var title: String? = null

    private var playbackSpeed = 1F
    private var position = 0
    private var isLink = false

    private val onBackPressedDispatcher by lazy { requireActivity().onBackPressedDispatcher }
    private val isInPictureInPictureMode by lazy { requireActivity().isInPictureInPictureMode }
    private val playerArgs: PlayerFragmentArgs by navArgs()


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

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun handleControls() {
        customControlsBinding.apply {
            if (isLink) goneViews(
                ivPlaybackSpeed,
                ivPrevious,
                ivNext,
                ivLock,
                ivUnlock,
                ivScaling,
                ivSeekForward,
                ivRewind
            )
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
        }
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(enabled = true) {
            override fun handleOnBackPressed() {
                exoplayer.stop()
                mediaSession.release()
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

    private fun finish() = findNavController().popBackStack()

    private fun retrieveWidthAndHeightAttributes(): Pair<Int, Int> {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(requireContext(), uri)
        val videoWidth =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
        val videoHeight =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
        "retrieveWidthAndHeightAttributes: width $videoWidth height $videoHeight".prettyPrint()
        return Pair(videoWidth ?: 16, videoHeight ?: 9)
    }

    private fun enterPIPMode() {
        val aspectRatio =
            Rational(
                retrieveWidthAndHeightAttributes().first,
                retrieveWidthAndHeightAttributes().second
            )
        val visibleRect = Rect()
        binding.exoplayerView.getGlobalVisibleRect(visibleRect)
        requireActivity().enterPictureInPictureMode(
            PictureInPictureParams.Builder().apply {
                setAspectRatio(aspectRatio)
                setSourceRectHint(visibleRect)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setAutoEnterEnabled(true)
                }
            }.build()
        )
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (isInPictureInPictureMode) binding.exoplayerView.hideController()
        else binding.exoplayerView.showController()
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun prepareExoplayer() {
        if (position == 0) customControlsBinding.ivPrevious.isEnabled = false
        else if (position == ControllerActivity.videoList.size - 1) customControlsBinding.ivNext.isEnabled =
            false
        binding.apply {
            val uri = Uri.parse(ControllerActivity.videoList[position].path)

            exoplayer = ExoPlayer.Builder(requireContext())
                .setSeekBackIncrementMs(Constants.SKIP_DURATION)
                .setSeekForwardIncrementMs(Constants.SKIP_DURATION)
                .build()

            repeat(ControllerActivity.videoList.size) {
                File(ControllerActivity.videoList[it].toString())
                mediaSource =
                    ProgressiveMediaSource.Factory(DefaultDataSource.Factory(requireContext()))
                        .createMediaSource(MediaItem.fromUri(uri))
            }

            concatenatingMediaSource =
                ConcatenatingMediaSource2.Builder()
                    .useDefaultMediaSourceFactory(requireContext())
                    .add(mediaSource, 0)
                    .add(MediaItem.fromUri(uri.toString()), 0)
                    .build()
            exoplayerView.apply {
                player = exoplayer
                keepScreenOn = true
                setControllerVisibilityListener(ControllerVisibilityListener {
                    if (isInPictureInPictureMode) hideController()
                })
            }
            exoplayer.apply {
                setMediaSource(concatenatingMediaSource)
                setPlaybackSpeed(playbackSpeed)
                prepare()
                seekTo(0, C.TIME_UNSET)
                videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
                play()
            }

            mediaSession = MediaSession.Builder(requireContext(), exoplayer).build()

            playerListeners()
        }
    }

    private fun playNext() {
        customControlsBinding.apply {
            try {
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
                mediaSession.release()
                exoplayer.release()
                finish()
                "Unable to play video".toToast(requireContext())
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_READY -> "onPlayWhenReadyChanged1: READY playWhenReady ".prettyPrint()
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
                if (!isInPictureInPictureMode && isPlaying)
                    customControlsBinding.ivPlay.setImageResource(R.drawable.ic_pause)
            }
        })
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
        if (!isLink) prepareExoplayer()
        else prepareExoPlayerForLink()
    }

    private fun prepareExoPlayerForLink() {
        exoplayer = ExoPlayer.Builder(requireContext()).build()
        binding.exoplayerView.player = exoplayer
        val mediaItem = MediaItem.fromUri(uri)
        exoplayer.apply {
            addMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

}