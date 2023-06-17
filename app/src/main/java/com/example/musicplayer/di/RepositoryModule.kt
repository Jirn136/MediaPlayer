package com.example.musicplayer.di

import com.example.musicplayer.repository.StorageRepository
import com.example.musicplayer.repository.StorageRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStorageRepository(repositoryImpl: StorageRepositoryImpl): StorageRepository

}