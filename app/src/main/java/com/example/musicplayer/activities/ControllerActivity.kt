package com.example.musicplayer.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityControllerBinding
import com.example.musicplayer.init.BaseActivity
import com.example.musicplayer.model.VideoDetails
import com.example.musicplayer.utils.clearFullScreen
import com.example.musicplayer.utils.setFullScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ControllerActivity : BaseActivity<ActivityControllerBinding>() {
    private lateinit var navController: NavController
    override fun inflateBinding(): ActivityControllerBinding =
        ActivityControllerBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.playerFragment) setFullScreen()
            else clearFullScreen()
        }
    }

    fun setScreenLandscape() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun setScreenPortrait() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun recreateActivity(){
        finishAndRemoveTask()
        startActivity(Intent.makeRestartActivityTask(ComponentName(this,ControllerActivity::class.java)))
    }

    companion object {
        var videoList: ArrayList<VideoDetails> = arrayListOf()
    }
}