package com.sebvideo.plexampplaylistwidget.widget

import android.content.Intent
import android.widget.RemoteViewsService

class PlaylistWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        PlaylistWidgetFactory(applicationContext, intent)
}
