package com.sebvideo.plexampplaylistwidget.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sebvideo.plexampplaylistwidget.data.AppDatabase
import com.sebvideo.plexampplaylistwidget.data.PlaylistEntity
import kotlinx.coroutines.launch

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).playlistDao()
    val playlists = dao.getAll()

    fun insert(name: String, modifiedUrl: String) = viewModelScope.launch {
        val count = dao.getAllSuspend().size
        dao.insert(PlaylistEntity(name = name, modifiedUrl = modifiedUrl, sortOrder = count))
    }

    fun delete(entity: PlaylistEntity) = viewModelScope.launch {
        dao.delete(entity)
    }

    fun updateName(entity: PlaylistEntity, newName: String) = viewModelScope.launch {
        dao.update(entity.copy(name = newName))
    }

    fun updateOrder(entities: List<PlaylistEntity>) = viewModelScope.launch {
        val updated = entities.mapIndexed { i, e -> e.copy(sortOrder = i) }
        dao.updateSortOrders(updated)
    }
}
