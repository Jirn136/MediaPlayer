package com.example.musicplayer.videoList.repository

import android.database.ContentObserver
import android.net.Uri
import com.example.musicplayer.model.VideoDetails


interface StorageRepository {
    suspend fun retrieveVideoDetails(): ArrayList<VideoDetails>

    suspend fun deleteVideos(contentUriList: List<Uri>)

    suspend fun registerObserver(observer: ContentObserver)

    suspend fun unRegisterObserver(observer: ContentObserver)
}