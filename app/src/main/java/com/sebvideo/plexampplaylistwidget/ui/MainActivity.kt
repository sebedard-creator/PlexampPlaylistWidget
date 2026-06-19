package com.sebvideo.plexampplaylistwidget.ui

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sebvideo.plexampplaylistwidget.R
import com.sebvideo.plexampplaylistwidget.databinding.ActivityMainBinding
import com.sebvideo.plexampplaylistwidget.utils.UrlTransformer
import com.sebvideo.plexampplaylistwidget.widget.PlaylistWidgetProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: PlaylistViewModel
    private lateinit var adapter: PlaylistAdapter

    private var nfcAdapter: NfcAdapter? = null
    private var nfcPendingIntent: PendingIntent? = null
    private var nfcScanDialog: AlertDialog? = null
    private var isWaitingForNfc = false

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
        setupRecyclerView()
        setupNfc()

        binding.fab.setOnClickListener { showNfcScanDialog() }

        viewModel.playlists.observe(this) { list ->
            adapter.setItems(list)
            notifyWidget()
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-enable dispatch if the activity was paused while the scan dialog was open.
        if (isWaitingForNfc) enableNfcDispatch()
    }

    override fun onPause() {
        super.onPause()
        // Must always be disabled in onPause per Android NFC documentation.
        nfcAdapter?.disableForegroundDispatch(this)
    }

    // ── RecyclerView ─────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = PlaylistAdapter(
            onEditClick = { item -> showRenameDialog(item) },
            onDeleteClick = { item -> showDeleteDialog(item) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0 // Disable swipe
        ) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                adapter.moveItemVisual(vh.adapterPosition, target.adapterPosition)
                return true
            }

            // Persist the new order only once the user releases the item.
            override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder) {
                super.clearView(rv, vh)
                viewModel.updateOrder(adapter.getItems())
            }

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                // Not used
            }
        }).attachToRecyclerView(binding.recyclerView)
    }

    private fun showRenameDialog(item: com.sebvideo.plexampplaylistwidget.data.PlaylistEntity) {
        val editText = EditText(this).apply {
            setText(item.name)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setPadding(64, 32, 64, 8)
        }
        AlertDialog.Builder(this)
            .setTitle("Renommer la playlist")
            .setView(editText)
            .setPositiveButton(R.string.sauvegarder) { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty() && newName != item.name) {
                    viewModel.updateName(item, newName)
                }
            }
            .setNegativeButton(R.string.annuler, null)
            .show()
    }

    private fun showDeleteDialog(item: com.sebvideo.plexampplaylistwidget.data.PlaylistEntity) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer")
            .setMessage("Voulez-vous vraiment supprimer '${item.name}' ?")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.delete(item)
            }
            .setNegativeButton(R.string.annuler, null)
            .show()
    }

    // ── NFC ──────────────────────────────────────────────────────────────────

    @SuppressLint("InlinedApi")
    private fun setupNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_MUTABLE else 0
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            flags
        )
    }

    private fun enableNfcDispatch() {
        val ndefMimeFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndefMimeFilter.addDataType("*/*")
        } catch (e: IntentFilter.MalformedMimeTypeException) { }

        val ndefHttpFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            addDataScheme("http")
        }
        val ndefHttpsFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            addDataScheme("https")
        }
        val tagFilter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)

        val filters = arrayOf(ndefMimeFilter, ndefHttpFilter, ndefHttpsFilter, tagFilter)
        nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, filters, null)
    }

    private fun showNfcScanDialog() {
        if (nfcAdapter?.isEnabled != true) {
            AlertDialog.Builder(this)
                .setTitle(R.string.nfc_indisponible)
                .setMessage(R.string.nfc_desactive_message)
                .setPositiveButton(R.string.ok, null)
                .show()
            return
        }
        isWaitingForNfc = true
        enableNfcDispatch()                          // enable immediately (activity is in foreground)
        nfcScanDialog = AlertDialog.Builder(this)
            .setTitle(R.string.scan_nfc_titre)
            .setMessage(R.string.scan_nfc_message)
            .setCancelable(false)
            .setNegativeButton(R.string.annuler) { _, _ ->
                isWaitingForNfc = false
                nfcAdapter?.disableForegroundDispatch(this)
            }
            .show()
    }

    @Suppress("DEPRECATION")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (!isWaitingForNfc) return
        if (intent.action != NfcAdapter.ACTION_NDEF_DISCOVERED &&
            intent.action != NfcAdapter.ACTION_TAG_DISCOVERED) return

        val msgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES) ?: return
        val ndefMsg = msgs[0] as? NdefMessage ?: return
        // NdefRecord.toUri() handles all URI-prefix bytes automatically (RFC 2396).
        val uri = ndefMsg.records.firstOrNull()?.toUri()?.toString() ?: return
        handleScannedUrl(uri)
    }

    private fun handleScannedUrl(rawUrl: String) {
        isWaitingForNfc = false
        nfcAdapter?.disableForegroundDispatch(this)
        nfcScanDialog?.dismiss()

        val transformedUrl = UrlTransformer.transform(rawUrl)
        if (transformedUrl == null) {
            AlertDialog.Builder(this)
                .setTitle(R.string.erreur)
                .setMessage(R.string.url_invalide_message)
                .setPositiveButton(R.string.ok, null)
                .show()
            return
        }

        val editText = EditText(this).apply {
            hint = getString(R.string.nom_playlist_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setPadding(64, 32, 64, 8)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.nom_playlist_titre)
            .setView(editText)
            .setPositiveButton(R.string.sauvegarder) { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) viewModel.insert(name, transformedUrl)
            }
            .setNegativeButton(R.string.annuler, null)
            .show()
    }

    // ── Widget notification ──────────────────────────────────────────────────

    private fun notifyWidget() {
        val manager = AppWidgetManager.getInstance(this)
        val ids = manager.getAppWidgetIds(ComponentName(this, PlaylistWidgetProvider::class.java))
        if (ids.isNotEmpty()) manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list_view)
    }
}
