package com.example.musicplayer.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicplayer.compose.ui.theme.MediaPlayerTheme
import com.example.musicplayer.model.VideoDetails
import com.example.musicplayer.utils.getFormattedDurationTime
import com.example.musicplayer.utils.toToast
import com.example.musicplayer.videoList.viewmodel.StorageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposeControllerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediaPlayerTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VideoList()
                }
            }
        }
    }

    @Preview
    @Composable
    private fun VideoList() {
        val viewModel = viewModel<StorageViewModel>()
        InitRecyclerView()
    }

    @Composable
    fun InitRecyclerView(storageModel: StorageViewModel = hiltViewModel()) {
        storageModel.retrieveVideoDetails()
        val details = storageModel.videoDetails.observeAsState()

        details.value?.let { detailList ->
            LazyColumn(content = {
                items(detailList) {
                    VideoItem(videoDetails = it)
                }
            })
        } ?: kotlin.run {
            LazyColumn(content = {
                items(listOf<VideoDetails>()) {
                    VideoItem(videoDetails = it)
                }
            })
        }
    }

    @Composable
    fun VideoItem(videoDetails: VideoDetails) {
        val context = LocalContext.current
        videoDetails.apply {
            Column(modifier = Modifier
                .fillMaxSize()
                .clickable {
                    "$title".toToast(context)
                }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, bottom = 5.dp, top = 10.dp)
                ) {
                    image?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(50.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .align(CenterVertically)
                    ) {
                        Text(
                            text = title, maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(0.6f)
                        )
                        Text(
                            text = getFormattedDurationTime(duration.toLong()).ifEmpty { "00:00" },
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .align(CenterEnd)

                        )

                    }
                }

                Divider(
                    thickness = 1.dp,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                )
            }
        }
    }
}