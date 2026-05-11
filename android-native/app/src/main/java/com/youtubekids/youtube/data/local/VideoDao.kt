package com.youtubekids.youtube.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE type = :type ORDER BY rowid DESC")
    fun getVideosByType(type: String): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Query("DELETE FROM videos WHERE id = :id AND type = :type")
    suspend fun deleteVideo(id: String, type: String)

    @Query("SELECT EXISTS(SELECT 1 FROM videos WHERE id = :id AND type = :type)")
    suspend fun isVideoInList(id: String, type: String): Boolean
}
