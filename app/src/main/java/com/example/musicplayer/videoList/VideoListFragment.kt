package com.example.musicplayer.videoList

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.R
import com.example.musicplayer.activities.ControllerActivity
import com.example.musicplayer.databinding.FragmentVideoListBinding
import com.example.musicplayer.di.MediaPreferences
import com.example.musicplayer.init.BaseFragment
import com.example.musicplayer.utils.Constants
import com.example.musicplayer.utils.gone
import com.example.musicplayer.utils.isNetConnected
import com.example.musicplayer.utils.prettyPrint
import com.example.musicplayer.utils.show
import com.example.musicplayer.utils.showViews
import com.example.musicplayer.utils.toToast
import com.example.musicplayer.videoList.adapter.VideoListAdapter
import com.example.musicplayer.videoList.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VideoListFragment : BaseFragment<FragmentVideoListBinding>() {
    @Inject
    lateinit var mediaPreferences: MediaPreferences
    private lateinit var videoAdapter: VideoListAdapter
    private val manifestPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_VIDEO
    else Manifest.permission.READ_EXTERNAL_STORAGE
    private val viewModel: StorageViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) viewModel.retrieveVideoDetails()
            else mediaPreferences.save(Constants.PERMISSION_ASKED_AND_DENIED, true)
        }

    private fun redirectToSettings() {
        "permission is required to load videos".toToast(requireContext())
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
                Uri.fromParts("package", requireContext().packageName, null)
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
        )
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentVideoListBinding = FragmentVideoListBinding.inflate(inflater)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        initRecyclerView()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        binding.swipeRefreshLayout.setOnRefreshListener {
            checkPermission()
        }
        if (isNetConnected(requireContext()))
            binding.toolbar.ivLink.setOnClickListener {
                createLinkDialog()
            }
        else binding.toolbar.ivLink.gone()
    }

    @SuppressLint("InflateParams")
    private fun createLinkDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_link, null)
        val linkText = dialogView.findViewById<EditText>(R.id.edtLink)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Link")
            .setPositiveButton("Ok") { dialog, _ ->
                if (linkText.text.trim().isEmpty())
                    "Enter valid link".toToast(requireContext())
                else
                    findNavController().navigate(
                        VideoListFragmentDirections.listToPlayer(
                            linkText.text.toString(),
                            Constants.EMPTY,
                            0, true
                        )
                    )
                    dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create().show()
    }

    private fun initObservers() {
        binding.apply {
            viewModel.videoDetails.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    "videoList: ${it.size}".prettyPrint()
                    tvPermission.gone()
                    showViews(rvVideoList, swipeRefreshLayout)
                    videoAdapter.submitList(it)
                    ControllerActivity.videoList.apply {
                        clear()
                        addAll(it)
                    }
                } else {
                    tvPermission.apply {
                        text = getString(R.string.get_video)
                    }
                }
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun initRecyclerView() {
        binding.rvVideoList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            videoAdapter = VideoListAdapter { details, position ->
                details.apply {
                    findNavController().navigate(
                        VideoListFragmentDirections.listToPlayer(
                            uri.toString(), title,
                            position, false
                        )
                    )
                }
            }
            adapter = videoAdapter
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.tvPermission.apply {
                binding.swipeRefreshLayout.gone()
                show()
                setOnClickListener {
                    if (!mediaPreferences.getBoolean(Constants.PERMISSION_ASKED_AND_DENIED)
                        || shouldShowRequestPermissionRationale(manifestPermission)
                    ) {
                        requestPermissionLauncher.launch(manifestPermission)
                    } else redirectToSettings()
                }
            }
        } else viewModel.retrieveVideoDetails()
    }
}