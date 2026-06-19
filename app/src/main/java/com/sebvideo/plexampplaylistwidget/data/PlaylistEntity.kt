package com.sebvideo.plexampplaylistwidget.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val modifiedUrl: String,
    val sortOrder: Int = 0
)
