package com.example.soundmixer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.soundmixer.R
import com.example.soundmixer.adapter.FilesAdapter.FileViewHolder
import com.google.android.material.textview.MaterialTextView

class FilesAdapter(
    private val fileNames: List<String>,
    private val onFileSelected: (String) -> Unit
) : RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {

    private val selectedItems = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_of_all_song, parent, false)
        return FileViewHolder(view, onFileSelected)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(fileNames[position], selectedItems.contains(fileNames[position]))
    }

    override fun getItemCount() = fileNames.size

    inner class FileViewHolder(
        itemView: View,
        private val onFileSelected: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvFileName: MaterialTextView = itemView.findViewById(R.id.tvFileName)
        private val selectionOverlay: View = itemView.findViewById(R.id.selectionOverlay)

        fun bind(fileName: String, isSelected: Boolean) {
            tvFileName.text = fileName
            selectionOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                if (selectedItems.contains(fileName)) {
                    selectedItems.remove(fileName)
                } else {
                    if (selectedItems.size < 2) {
                        selectedItems.add(fileName)
                    } else {
                        Toast.makeText(itemView.context, "You can select only two files", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
                onFileSelected(fileName)
                notifyItemChanged(adapterPosition)
            }
        }
    }
}

/*class FilesAdapter(
    private val fileNames: List<String>,
    private val onFileSelected: (String) -> Unit
) : RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_of_all_song, parent, false)
        return FileViewHolder(view, onFileSelected)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(fileNames[position])
    }

    override fun getItemCount() = fileNames.size

    class FileViewHolder(
        itemView: android.view.View,
        private val onFileSelected: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvFileName: android.widget.TextView = itemView.findViewById(R.id.tvFileName)

        fun bind(fileName: String) {
            tvFileName.text = fileName
            itemView.setOnClickListener {
                onFileSelected(fileName)
                itemView.isSelected = !itemView.isSelected
            }
        }
    }
}*/
