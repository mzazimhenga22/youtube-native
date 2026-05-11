package com.youtubekids.youtube.di

import android.content.Context
import androidx.room.Room
import com.youtubekids.youtube.data.local.AppDatabase
import com.youtubekids.youtube.data.local.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "youtube_tv.db"
        ).build()
    }

    @Provides
    fun provideVideoDao(database: AppDatabase): VideoDao {
        return database.videoDao()
    }
}
