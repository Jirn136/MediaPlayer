package com.example.musicplayer.repository

import com.example.musicplayer.model.VideoDetails


interface StorageRepository {
    suspend fun retrieveVideoDetails(): ArrayList<VideoDetails>
}