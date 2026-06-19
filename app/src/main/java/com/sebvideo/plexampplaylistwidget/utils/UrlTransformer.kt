package com.sebvideo.plexampplaylistwidget.utils

import android.net.Uri

/**
 * Transforms a Plexamp NFC share URL into a functional playMedia URL with shuffle=1.
 *
 * Input:
 *   https://listen.plex.tv/com.plexapp.agents.none:/[UUID]?source=[MACHINE_ID]&key=[METADATA_KEY]
 *
 * Output:
 *   https://listen.plex.tv/player/playback/playMedia
 *       ?uri=server://[MACHINE_ID]/com.plexapp.plugins.library[METADATA_KEY]&shuffle=1
 *
 * The /items suffix is stripped from the decoded key if present.
 */
object UrlTransformer {

    fun transform(inputUrl: String): String? {
        return try {
            val uri      = Uri.parse(inputUrl)
            val machineId = uri.getQueryParameter("source") ?: return null
            val rawKey    = uri.getQueryParameter("key")    ?: return null
            val cleanKey  = rawKey.removeSuffix("/items")
            
            val builder = Uri.parse("https://listen.plex.tv/player/playback/playMedia").buildUpon()
            builder.appendQueryParameter("uri", "server://${machineId}/com.plexapp.plugins.library${cleanKey}")
            builder.appendQueryParameter("shuffle", "1")
            builder.build().toString()
        } catch (e: Exception) {
            null
        }
    }
}
