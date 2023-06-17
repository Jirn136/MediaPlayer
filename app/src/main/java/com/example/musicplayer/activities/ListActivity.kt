package com.example.musicplayer.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.R
import com.example.musicplayer.adapter.VideoListAdapter
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.di.MediaPreferences
import com.example.musicplayer.model.VideoDetails
import com.example.musicplayer.utils.Constants
import com.example.musicplayer.utils.gone
import com.example.musicplayer.utils.show
import com.example.musicplayer.utils.toToast
import com.example.musicplayer.viewmodel.StorageViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var mediaPreferences: MediaPreferences

    private val viewModel: StorageViewModel by viewModels()
    private val manifestPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_VIDEO
    else Manifest.permission.READ_EXTERNAL_STORAGE

    private var fromRefresh: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initObserver()
        binding.toolbar.ivRefresh.setOnClickListener {
            fromRefresh = true
            verifyReadPermission()
        }

    }

    private fun verifyReadPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, manifestPermission
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("permission", "verifyReadPermission: 1")
                if (fromRefresh) {
                    "Refreshing".toToast(this)
                    fromRefresh = false
                }
                binding.toolbar.ivRefresh.show()
                mediaPreferences.save(Constants.PERMISSION_ASKED_AND_DENIED, false)
                viewModel.retrieveVideoDetails()
            }

            shouldShowRequestPermissionRationale(manifestPermission) -> {
                Log.i("permission", "verifyReadPermission: 2")
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Need permission for accessing Videos",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Ok") {
                    mediaPreferences.save(Constants.PERMISSION_ASKED_AND_DENIED, true)
                    Log.i(
                        "permission",
                        "verifyReadPermission: snackBar ${mediaPreferences.getBoolean(Constants.PERMISSION_ASKED_AND_DENIED)}"
                    )
                    binding.toolbar.ivRefresh.gone()
                    requestPermissionLauncher.launch(manifestPermission)
                }.show()
            }

            mediaPreferences.getBoolean(Constants.PERMISSION_ASKED_AND_DENIED) -> {
                Log.i("permission", "verifyReadPermission: 3")
                "Permission mandatory for listing videos".toToast(this)
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
                        Uri.fromParts("package", packageName, null)
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    }
                )
            }

            else -> {
                mediaPreferences.save(Constants.PERMISSION_ASKED_AND_DENIED, true)
                Log.i(
                    "permission",
                    "verifyReadPermission:else ${mediaPreferences.getBoolean(Constants.PERMISSION_ASKED_AND_DENIED)}"
                )
                binding.toolbar.ivRefresh.gone()
                requestPermissionLauncher.launch(manifestPermission)
            }

        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) viewModel.retrieveVideoDetails()

        }


    private fun initRecyclerView(videoList: ArrayList<VideoDetails>) = binding.rvVideoList.apply {
        show()
        binding.tvPermission.gone()
        layoutManager = LinearLayoutManager(this@ListActivity)
        val videoAdapter = VideoListAdapter(videoList) { details ->
            startActivity(Intent(this@ListActivity, PlayerActivity::class.java).apply {
                details.apply {
                    putExtra(Constants.URI, uri.toString())
                    putExtra(Constants.TITLE, title)
                }
            })
            Log.i("Adapter", "initRecyclerView: ${Gson().toJson(details)}")
        }
        adapter = videoAdapter
    }


    private fun initObserver() {
        viewModel.videoDetails.observe(this) {
            if (it.isNotEmpty()) {
                Log.i("Observer", "onCreate: ${Gson().toJson(it)}")
                initRecyclerView(it)
            } else {
                binding.tvPermission.apply {
                    show()
                    text = getString(R.string.get_video)
                }
                binding.rvVideoList.gone()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                this,
                manifestPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
            verifyReadPermission()
        else
            binding.tvPermission.setOnClickListener {
                verifyReadPermission()
            }

    }

}