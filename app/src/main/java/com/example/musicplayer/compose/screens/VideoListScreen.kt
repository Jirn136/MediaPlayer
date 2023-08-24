package com.example.musicplayer.compose.screens

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.musicplayer.R
import com.example.musicplayer.model.VideoDetails
import com.example.musicplayer.utils.getFormattedDurationTime
import com.example.musicplayer.utils.toToast
import com.example.musicplayer.videoList.viewmodel.StorageViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VideoListScreen(
    deleteList: SnapshotStateList<Uri>,
    deleteEnabled: MutableState<Boolean>,
    storageModel: StorageViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Video List") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(
                        0xFFD0BCFF
                    )
                ),
                actions = {
                    if (deleteEnabled.value) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "delete",
                            modifier = Modifier.clickable {
                                storageModel.deleteVideo(deleteList)
                                deleteEnabled.value = false
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
            )
        }
    ) {
        Surface(
            modifier = Modifier
                .consumedWindowInsets(it)
                .padding(it),
            color = MaterialTheme.colorScheme.background
        ) {
            VideoList(
                deleteList = deleteList,
                deleteEnabled = deleteEnabled,
                storageModel = storageModel
            )
        }
    }
}

@Composable
private fun VideoList(
    deleteList: SnapshotStateList<Uri>,
    deleteEnabled: MutableState<Boolean>,
    storageModel: StorageViewModel
) {
    InitRecyclerView(
        deleteList = deleteList,
        deleteEnabled = deleteEnabled,
        storageModel = storageModel
    )
}

@Composable
fun InitRecyclerView(
    deleteList: SnapshotStateList<Uri>,
    deleteEnabled: MutableState<Boolean>,
    storageModel: StorageViewModel
) {
    val details = storageModel.videoDetailsState.value

    details.let { detailList ->
        LazyColumn(content = {
            items(detailList.size) {
                VideoItem(
                    deleteList = deleteList,
                    deleteEnabled = deleteEnabled,
                    videoDetails = details[it]
                )
            }
        })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoItem(
    deleteList: SnapshotStateList<Uri>,
    deleteEnabled: MutableState<Boolean>,
    videoDetails: VideoDetails
) {
    val context = LocalContext.current

    videoDetails.apply {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onLongClick = {
                        deleteEnabled.value = true
                        deleteList.add(uri)
                    },
                    enabled = true,
                ) {
                    if (deleteEnabled.value)
                        addOrRemoveFromList(deleteList, uri)
                    "$title".toToast(context)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, bottom = 5.dp, top = 10.dp)
            ) {
                val isSelected = deleteList.contains(uri)

                if (deleteEnabled.value) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            addOrRemoveFromList(deleteList, uri)
                        }
                    )
                }
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
                        .align(Alignment.CenterVertically)
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
                            .align(Alignment.CenterEnd)
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

fun addOrRemoveFromList(deleteList: SnapshotStateList<Uri>, uri: Uri) {
    if (deleteList.contains(uri))
        deleteList.remove(uri)
    else deleteList.add(uri)
}