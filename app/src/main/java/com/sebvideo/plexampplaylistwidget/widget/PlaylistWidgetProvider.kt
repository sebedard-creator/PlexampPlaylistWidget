package com.sebvideo.plexampplaylistwidget.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import com.sebvideo.plexampplaylistwidget.R

class PlaylistWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    companion object {
        @SuppressLint("InlinedApi")
        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Each widget gets a unique Intent (data URI encodes the widgetId).
            val serviceIntent = Intent(context, PlaylistWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            @Suppress("DEPRECATION")
            views.setRemoteAdapter(R.id.widget_list_view, serviceIntent)
            views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_text)

            // PendingIntentTemplate — the fill-in extras are provided per-row by the factory.
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            val pendingIntent = PendingIntent.getActivity(
                context, 0, Intent(context, WidgetClickActivity::class.java), flags
            )
            views.setPendingIntentTemplate(R.id.widget_list_view, pendingIntent)

            manager.updateAppWidget(widgetId, views)
        }
    }
}
