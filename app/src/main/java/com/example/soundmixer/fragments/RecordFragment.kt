package com.example.soundmixer.fragments

import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.soundmixer.R
import java.io.File

class RecordFragment : Fragment() {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFilePath: String = ""
    private var isRecording = false

    private var recordingStartTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        } else {
            // granted

        }
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playButton = view.findViewById<ImageButton>(R.id.btnPlay)
        val stopButton = view.findViewById<ImageButton>(R.id.btnStop)

        val tvTimer = view.findViewById<TextView>(R.id.tvTimer)
        val tvRecordingNow = view.findViewById<TextView>(R.id.tvRecordingNow)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val customDir = File(downloadsDir, "SoundMixer")

        if (!customDir.exists()) {
            customDir.mkdirs()
        }

        outputFilePath = generateUniqueFilePath(customDir)

        setUpRecording(playButton, stopButton, tvTimer, tvRecordingNow)
    }

    private fun generateUniqueFilePath(directory: File): String {
        val timestamp = System.currentTimeMillis()
        return File(directory, "audio_recording_$timestamp.mp3").absolutePath
    }

    private fun setUpRecording(playButton: ImageButton, stopButton: ImageButton, tvTimer: TextView, tvRecordingNow: TextView) {
        playButton.setOnClickListener {
            if (!isRecording) {
                startRecording(tvTimer, tvRecordingNow)
            }
        }

        stopButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            }
        }
    }

    private fun startRecording(tvTimer: TextView, tvRecordingNow: TextView) {
        mediaRecorder = MediaRecorder().apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFilePath)
                prepare()
                start()
                isRecording = true
                recordingStartTime = System.currentTimeMillis()

                tvTimer.visibility = View.VISIBLE
                tvRecordingNow.visibility = View.VISIBLE

                timerRunnable = object : Runnable {
                    override fun run() {
                        val elapsedTime = System.currentTimeMillis() - recordingStartTime
                        val seconds = (elapsedTime / 1000) % 60
                        val minutes = (elapsedTime / (1000 * 60)) % 60
                        tvTimer.text = String.format("%02d:%02d", minutes, seconds)
                        handler.postDelayed(this, 1000)
                    }
                }
                handler.post(timerRunnable)

                Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to start recording: ${e.message}", Toast.LENGTH_LONG).show()
                release()
                mediaRecorder = null
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false

        view?.findViewById<TextView>(R.id.tvRecordingNow)?.visibility = View.GONE

        handler.removeCallbacks(timerRunnable)

        Toast.makeText(context, "Recording stopped", Toast.LENGTH_SHORT).show()
    }

}
