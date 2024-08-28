package com.example.soundmixer.adapter

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.soundmixer.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import java.io.File
import java.io.IOException

class PlayFilesAdapter(
    private val fileNames: List<String>,
    private val mergedFilesDir: File
) : RecyclerView.Adapter<PlayFilesAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_of_merged_files, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(fileNames[position])
    }

    override fun getItemCount() = fileNames.size

    inner class FileViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvFileName: MaterialTextView = itemView.findViewById(R.id.tvFileName)
        private val btnPlay: MaterialButton = itemView.findViewById(R.id.btnPlay)
        fun bind(fileName: String) {
            tvFileName.text = fileName

            btnPlay.setOnClickListener {
                val context = itemView.context
                Toast.makeText(context, "Please wait, song is loading...", Toast.LENGTH_SHORT).show()
                val fileToPlay = File(mergedFilesDir, fileName)
                playAudio(fileToPlay)
            }
        }

        private fun playAudio(file: File) {
            val mediaPlayer = MediaPlayer()
            try {
                mediaPlayer.setDataSource(file.absolutePath)
                mediaPlayer.prepare()
                mediaPlayer.start()
            } catch (e: IOException) {
                Toast.makeText(itemView.context, "Error playing file", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
