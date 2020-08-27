package com.simform.risetestapp.view.activity

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.REPEAT_MODE_ALL
import com.google.android.exoplayer2.Player.REPEAT_MODE_OFF
import com.google.android.exoplayer2.Player.REPEAT_MODE_ONE
import com.google.android.exoplayer2.Player.STATE_BUFFERING
import com.google.android.exoplayer2.Player.STATE_ENDED
import com.google.android.exoplayer2.Player.STATE_IDLE
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.simform.risetestapp.R
import com.simform.risetestapp.base.BaseActivity
import com.simform.risetestapp.databinding.ActivityPlayVideoBinding
import com.simform.risetestapp.util.extention.ActivityExtensions.toast
import kotlinx.android.synthetic.main.activity_play_video.btnChangeRepeatMode
import kotlinx.android.synthetic.main.activity_play_video.playerView
import kotlinx.android.synthetic.main.activity_play_video.progressBar

class PlayVideoActivity : BaseActivity<ActivityPlayVideoBinding>() {

    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private val TAG = "PlayVideoActivity"

    override fun getLayoutResId(): Int = R.layout.activity_play_video

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    private fun initializePlayer() {

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this)

        mediaDataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"))

        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse("file:///android_asset/Ocean_waves_with_reflection_of_sun.mp4"))
        //val mediaSource = ExtractorMediaSource(Uri.parse("file:///android_asset/Ocean_waves_with_reflection_of_sun.mp4"), mediaDataSourceFactory, DefaultExtractorsFactory(), null, null)

        simpleExoPlayer.prepare(mediaSource, false, false)
        simpleExoPlayer.playWhenReady = true

        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.player = simpleExoPlayer
        playerView.requestFocus()

        /** Default repeat mode is REPEAT_MODE_OFF */
        btnChangeRepeatMode.setOnClickListener {
            if (simpleExoPlayer.repeatMode == REPEAT_MODE_OFF)
                simpleExoPlayer.repeatMode = REPEAT_MODE_ONE
            else if (simpleExoPlayer.repeatMode == REPEAT_MODE_ONE) {
                simpleExoPlayer.repeatMode = REPEAT_MODE_ALL
            } else {
                simpleExoPlayer.repeatMode = REPEAT_MODE_OFF
            }
        }


        simpleExoPlayer.addListener(object : Player.EventListener {

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                Log.d(TAG, "onPlaybackParametersChanged: ")
            }

            override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
                Log.d(TAG, "onTracksChanged: ")
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                Log.d(TAG, "onPlayerError: ")
            }

            /** 4 playbackState exists */
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    STATE_BUFFERING -> {
                        progressBar.visibility = View.VISIBLE
                        Log.d(TAG, "onPlayerStateChanged - STATE_BUFFERING")
                        toast("onPlayerStateChanged - STATE_BUFFERING")
                    }
                    STATE_READY -> {
                        progressBar.visibility = View.INVISIBLE
                        Log.d(TAG, "onPlayerStateChanged - STATE_READY")
                        toast("onPlayerStateChanged - STATE_READY")
                    }
                    STATE_IDLE -> {
                        Log.d(TAG, "onPlayerStateChanged - STATE_IDLE")
                        toast("onPlayerStateChanged - STATE_IDLE")
                    }
                    STATE_ENDED -> {
                        Log.d(TAG, "onPlayerStateChanged - STATE_ENDED")
                        toast("onPlayerStateChanged - STATE_ENDED")
                    }
                }
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                Log.d(TAG, "onLoadingChanged: ")
            }

            override fun onPositionDiscontinuity(reason: Int) {
                Log.d(TAG, "onPositionDiscontinuity: ")
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                Log.d(TAG, "onRepeatModeChanged: ")
                Toast.makeText(baseContext, "repeat mode changed", Toast.LENGTH_SHORT).show()
            }

            override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
                Log.d(TAG, "onTimelineChanged: ")
            }
        })
    }

    private fun releasePlayer() {
        simpleExoPlayer.release()
    }

    public override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart: ")
        if (Util.SDK_INT > 23) initializePlayer()
    }

    public override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume: ")
        if (Util.SDK_INT <= 23) initializePlayer()
    }

    public override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: ")
        if (Util.SDK_INT <= 23) releasePlayer()
    }

    public override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: ")
        if (Util.SDK_INT > 23) releasePlayer()
    }

    companion object {
        const val STREAM_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
    }
}