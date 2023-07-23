package com.example.musicplayer.videoList.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.model.VideoDetails
import com.example.musicplayer.videoList.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(private val storageRepository: StorageRepository) :
    ViewModel() {
    val videoDetails: LiveData<ArrayList<VideoDetails>> get() = mVideoDetails
    private val mVideoDetails = MutableLiveData<ArrayList<VideoDetails>>()

    fun retrieveVideoDetails() {
        viewModelScope.launch {
            mVideoDetails.apply {
                postValue(storageRepository.retrieveVideoDetails())
            }
        }
    }
}