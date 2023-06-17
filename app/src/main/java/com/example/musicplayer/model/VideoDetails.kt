package com.example.musicplayer.model

import android.graphics.Bitmap
import android.net.Uri

data class VideoDetails(
    val title: String,
    val path: String,
    val image: Bitmap?,
    val uri: Uri,
    val duration:String
)
