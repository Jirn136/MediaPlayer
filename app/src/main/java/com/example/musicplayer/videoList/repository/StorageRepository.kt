package com.example.musicplayer.videoList.repository

import com.example.musicplayer.model.VideoDetails


interface StorageRepository {
    suspend fun retrieveVideoDetails(): ArrayList<VideoDetails>
}