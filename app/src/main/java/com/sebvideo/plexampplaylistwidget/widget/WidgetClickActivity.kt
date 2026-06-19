package com.sebvideo.plexampplaylistwidget.widget

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sebvideo.plexampplaylistwidget.R

/**
 * Invisible trampoline activity launched by the widget's PendingIntentTemplate.
 * Reads the fill-in extras, shows a Toast, fires the ACTION_VIEW intent, then finishes.
 */
class WidgetClickActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url  = intent.getStringExtra("playlist_url")
        val name = intent.getStringExtra("playlist_name") ?: ""

        if (!url.isNullOrBlank()) {
            Toast.makeText(
                this,
                getString(R.string.lancement_playlist, name),
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }
        finish()
    }
}
