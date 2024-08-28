package com.example.soundmixer.adapter

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.soundmixer.R
import com.example.soundmixer.service.MusicService
import com.example.soundmixer.model.SoundDetails
import java.io.File
import java.io.IOException

class SongsAdapter(private val songs: MutableList<SoundDetails>) :
    RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var playingPosition: Int? = null

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val btnPlay: TextView = view.findViewById(R.id.btnPlay)
        val tvSongName: TextView = view.findViewById(R.id.tvSongName)
        val btnDownload: TextView = view.findViewById(R.id.btnDownload)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val currentPosition = holder.adapterPosition
        if (currentPosition == RecyclerView.NO_POSITION) {
            return
        }

        val song = songs[currentPosition]
        holder.tvSongName.text = song.name

        holder.btnPlay.setOnClickListener {
            val context = holder.itemView.context
            Toast.makeText(context, "Please wait, song is loading...", Toast.LENGTH_SHORT).show()

            val action = if (playingPosition != null && playingPosition == currentPosition) {
                MusicService.ACTION_PAUSE
            } else {
                MusicService.ACTION_PLAY
            }

            val intent = Intent(context, MusicService::class.java).apply {
                this.action = action
                putExtra(MusicService.EXTRA_MEDIA_URL, song.previews.previewHqMp3)
            }

            if (action == MusicService.ACTION_PLAY) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }

            playingPosition = if (action == MusicService.ACTION_PLAY) currentPosition else null
        }


        holder.btnDownload.setOnClickListener {
            val context = holder.itemView.context
            val songName = song.name

            val request = DownloadManager.Request(Uri.parse(song.previews.previewHqMp3))
                .setTitle(songName)
                .setDescription("Downloading $songName")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "${songName}.mp3"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val downloadId = downloadManager.enqueue(request)

            Toast.makeText(context, "Downloading $songName", Toast.LENGTH_SHORT).show()

            val onComplete = object : BroadcastReceiver() {
                @SuppressLint("Range")
                override fun onReceive(context: Context?, intent: Intent?) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val status =
                            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            val downloadedFileUri =
                                cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                            val downloadedFile = Uri.parse(downloadedFileUri).path?.let { File(it) }

                            val soundMixerDir = File(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                "SoundMixer"
                            )
                            if (!soundMixerDir.exists()) {
                                soundMixerDir.mkdirs()
                            }
                            val newFile = File(soundMixerDir, "${songName}.mp3")

                            try {
                                downloadedFile?.let { file ->
                                    if (file.renameTo(newFile)) {
                                        Toast.makeText(
                                            context,
                                            "Moved to SoundMixer",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Failed to move file",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                                Toast.makeText(
                                    context,
                                    "Error moving file: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    cursor.close()
                }
            }

            context.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        }


    }

    override fun getItemCount(): Int = songs.size

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }


}
