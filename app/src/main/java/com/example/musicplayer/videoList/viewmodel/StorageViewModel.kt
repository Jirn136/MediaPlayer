package com.example.musicplayer.videoList.viewmodel

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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

    // Below 2 lines are not required for compose
    val videoDetails: LiveData<ArrayList<VideoDetails>> get() = mVideoDetails
    private val mVideoDetails = MutableLiveData<ArrayList<VideoDetails>>()

    val videoDetailsState: State<List<VideoDetails>> get() = _videoDetailsState
    private val _videoDetailsState = mutableStateOf(listOf<VideoDetails>())

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            this.onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            viewModelScope.launch {
                _videoDetailsState.value = storageRepository.retrieveVideoDetails()
            }
        }
    }

    init {
        retrieveVideoDetails()
    }

    fun retrieveVideoDetails() {
        viewModelScope.launch {
            _videoDetailsState.value = storageRepository.retrieveVideoDetails()
            storageRepository.registerObserver(observer)
        }
    }

    fun deleteVideo(uriList: List<Uri>) {
        viewModelScope.launch {
            storageRepository.deleteVideos(uriList)
        }
    }

    fun unRegisterObserver() {
        viewModelScope.launch {
            storageRepository.unRegisterObserver(observer)
        }
    }

}