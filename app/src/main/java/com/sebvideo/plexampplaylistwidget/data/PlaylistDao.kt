package com.sebvideo.plexampplaylistwidget.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
abstract class PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY sortOrder ASC")
    abstract fun getAll(): LiveData<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists ORDER BY sortOrder ASC")
    abstract suspend fun getAllSuspend(): List<PlaylistEntity>

    @Insert
    abstract suspend fun insert(entity: PlaylistEntity)

    @Delete
    abstract suspend fun delete(entity: PlaylistEntity)

    @Update
    abstract suspend fun update(entity: PlaylistEntity)

    @Transaction
    open suspend fun updateSortOrders(entities: List<PlaylistEntity>) {
        entities.forEach { update(it) }
    }
}
