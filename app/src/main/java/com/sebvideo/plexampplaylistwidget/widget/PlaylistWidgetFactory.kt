package com.sebvideo.plexampplaylistwidget.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.sebvideo.plexampplaylistwidget.R
import com.sebvideo.plexampplaylistwidget.data.AppDatabase
import com.sebvideo.plexampplaylistwidget.data.PlaylistEntity
import kotlinx.coroutines.runBlocking

class PlaylistWidgetFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private val dao = AppDatabase.getInstance(context).playlistDao()
    private var playlists: List<PlaylistEntity> = emptyList()

    override fun onCreate()         { loadData() }
    override fun onDataSetChanged() { loadData() }   // called on a bg thread — runBlocking is safe
    override fun onDestroy()        {}

    private fun loadData() {
        playlists = runBlocking { dao.getAllSuspend() }
    }

    override fun getCount() = playlists.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position < 0 || position >= playlists.size) {
            return RemoteViews(context.packageName, R.layout.widget_list_item)
        }
        val item = playlists[position]
        return RemoteViews(context.packageName, R.layout.widget_list_item).apply {
            setTextViewText(R.id.widget_item_text, item.name)
            // fill-in extras merge with the PendingIntentTemplate set in the provider.
            val fill = Intent().apply {
                putExtra("playlist_url",  item.modifiedUrl)
                putExtra("playlist_name", item.name)
            }
            setOnClickFillInIntent(R.id.widget_item_text, fill)
        }
    }

    override fun getLoadingView()    = null
    override fun getViewTypeCount()  = 1
    override fun getItemId(pos: Int) =
        if (pos < playlists.size) playlists[pos].id.toLong() else pos.toLong()
    override fun hasStableIds()      = true
}
