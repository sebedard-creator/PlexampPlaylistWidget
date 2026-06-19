package com.sebvideo.plexampplaylistwidget.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sebvideo.plexampplaylistwidget.data.PlaylistEntity
import com.sebvideo.plexampplaylistwidget.databinding.ItemPlaylistBinding
import java.util.Collections

class PlaylistAdapter(
    private val onEditClick: (PlaylistEntity) -> Unit,
    private val onDeleteClick: (PlaylistEntity) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    private val items = mutableListOf<PlaylistEntity>()

    inner class ViewHolder(val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.textName.text = item.name
        holder.binding.btnEdit.setOnClickListener { onEditClick(item) }
        holder.binding.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = items.size

    /** Called by LiveData observer — uses DiffUtil for smooth updates. */
    fun setItems(newItems: List<PlaylistEntity>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(op: Int, np: Int) = items[op].id == newItems[np].id
            override fun areContentsTheSame(op: Int, np: Int) = items[op] == newItems[np]
        })
        items.clear()
        items.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }

    /** Visual-only move during drag; persisted via clearView -> viewModel.updateOrder. */
    fun moveItemVisual(from: Int, to: Int) {
        Collections.swap(items, from, to)
        notifyItemMoved(from, to)
    }

    fun getItems(): List<PlaylistEntity> = items.toList()
    fun getItemAt(position: Int): PlaylistEntity = items[position]
}
