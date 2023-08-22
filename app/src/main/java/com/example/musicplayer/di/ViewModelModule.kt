package com.example.musicplayer.di

import com.example.musicplayer.videoList.repository.StorageRepository
import com.example.musicplayer.videoList.viewmodel.StorageViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideStorageViewModel(storageRepository: StorageRepository): StorageViewModel =
        StorageViewModel(storageRepository)
}