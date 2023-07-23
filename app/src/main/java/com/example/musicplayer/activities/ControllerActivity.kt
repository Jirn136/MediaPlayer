package com.example.musicplayer.activities

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityControllerBinding
import com.example.musicplayer.init.BaseActivity
import com.example.musicplayer.model.VideoDetails
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
            if (destination.id == R.id.playerFragment) {
                setFullScreen()
            }
        }
    }

    companion object {
        var videoList: ArrayList<VideoDetails> = arrayListOf()
    }
}