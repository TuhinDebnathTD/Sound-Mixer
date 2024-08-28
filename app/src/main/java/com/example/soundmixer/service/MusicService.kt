package com.example.soundmixer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.soundmixer.R
import com.example.soundmixer.activities.MainActivity

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MusicService", "onStartCommand called with action: ${intent?.action}")
        when (intent?.action) {
            ACTION_PLAY -> {
                val mediaUrl = intent.getStringExtra(EXTRA_MEDIA_URL)
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                    mediaPlayer?.reset()
                }
                playMusic(mediaUrl)
            }
            ACTION_PAUSE -> pauseMusic()
            ACTION_STOP -> {
                Log.d("MusicService", "Stop action received")
                mediaPlayer?.stop()
                isPlaying = false
                showNotification(isPlaying = false)
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private var isPlaying = false

    private fun playMusic(mediaUrl: String?) {
        Log.d("MusicService", "playMusic called with URL: $mediaUrl")
        if (mediaUrl.isNullOrEmpty()) {
            Log.e("MusicService", "Media URL is null or empty")
            return
        }

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(mediaUrl)
            setOnPreparedListener {
                start()
                this@MusicService.isPlaying = true
                showNotification(isPlaying = true)
                Log.d("MusicService", "Music started")
            }
            setOnErrorListener { mp, what, extra ->
                Log.e("MusicService", "MediaPlayer error: what=$what, extra=$extra")
                this@MusicService.isPlaying = false
                showNotification(isPlaying = false)
                true
            }
            setOnCompletionListener {
                Log.d("MusicService", "Music completed")
                this@MusicService.isPlaying = false
                showNotification(isPlaying = false)
            }
            prepareAsync()
        }
    }

    private fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
                showNotification(isPlaying = false)
                Log.d("MusicService", "Music paused")
            } else {
                Log.d("MusicService", "Music was not playing")
            }
        }
    }


    private fun showNotification(isPlaying: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Music Player Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_pause, "",
                getServicePendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play, "",
                getServicePendingIntent(ACTION_PLAY)
            )
        }

        val stopAction = NotificationCompat.Action(
            R.drawable.ic_pause, "Dismiss",
            getServicePendingIntent(ACTION_STOP)
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText(if (isPlaying) "Playing the song" else "")
            .setSmallIcon(R.drawable.baseline_3d_rotation_24)
            .setContentIntent(pendingIntent)
            .addAction(playPauseAction)
            .addAction(stopAction)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        Log.d("MusicService", "Notification should now be visible")
    }


    private fun getServicePendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }

        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "MusicServiceChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_MEDIA_URL = "EXTRA_MEDIA_URL"
    }
}

