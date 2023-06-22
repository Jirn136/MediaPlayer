package com.example.musicplayer.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoDetails(
    val title: String,
    val path: String,
    val image: Bitmap?,
    val uri: Uri,
    val duration: String,
) : Parcelable
