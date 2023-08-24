package com.example.musicplayer.compose

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.musicplayer.compose.screens.VideoListScreen
import com.example.musicplayer.compose.ui.theme.MediaPlayerTheme
import com.example.musicplayer.videoList.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposeControllerActivity : ComponentActivity() {

    private val storageModel: StorageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val deleteList = remember {
                mutableStateListOf<Uri>()
            }
            val deleteEnabled = remember {
                mutableStateOf(false)
            }
            val listOrGrid = remember {
                mutableStateOf(false)
            }
            if (deleteList.isEmpty())
                deleteEnabled.value = false

            BackHandler(enabled = true) {
                if (deleteEnabled.value)
                    deleteEnabled.value = false
            }
            MediaPlayerTheme {
                // A surface container using the 'background' color from the theme
                VideoListScreen(
                    deleteList = deleteList,
                    listOrGrid = listOrGrid,
                    deleteEnabled = deleteEnabled,
                    storageModel = storageModel
                )
            }
        }
    }

    override fun onDestroy() {
        storageModel.unRegisterObserver()
        super.onDestroy()
    }
}