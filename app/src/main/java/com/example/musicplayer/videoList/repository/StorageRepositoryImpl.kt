package com.example.musicplayer.videoList.repository

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.example.musicplayer.model.VideoDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject


class StorageRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
    StorageRepository {
    @SuppressLint("Range")
    override suspend fun retrieveVideoDetails(): ArrayList<VideoDetails> {
        val videoItems: ArrayList<VideoDetails> = ArrayList()

        val contentResolver: ContentResolver = context.contentResolver
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val cursor = contentResolver.query(uri, null, null, null, null)

        //looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            try {
                do {
                    val title =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                    )
                    val path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val duration =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                    var thumbnail: Bitmap? = null
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            thumbnail = context.contentResolver.loadThumbnail(
                                contentUri, Size(640, 480), null
                            )
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val videoModel = thumbnail?.let {
                        VideoDetails(
                            title = title,
                            image = it,
                            path = path,
                            uri = contentUri,
                            duration = duration
                        )
                    } ?: VideoDetails(
                        title = title,
                        image = null,
                        path = path,
                        uri = contentUri,
                        duration = duration
                    )
                    videoItems.add(videoModel)
                } while (cursor.moveToNext())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor.close()
            }
        }
        return videoItems
    }

    override suspend fun deleteVideos(contentUriList: List<Uri>) {
        contentUriList.forEach {
            context.contentResolver.delete(it, null, null)
        }
    }

    override suspend fun registerObserver(observer: ContentObserver) {
        context.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )
    }

    override suspend fun unRegisterObserver(observer: ContentObserver) {
        context.contentResolver.unregisterContentObserver(
            observer
        )
    }
}