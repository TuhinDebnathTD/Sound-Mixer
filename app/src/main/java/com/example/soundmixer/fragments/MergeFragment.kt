package com.example.soundmixer.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.soundmixer.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import java.io.File

class MergeFragment : Fragment() {

    private var progressDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_merge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvPrompt = view.findViewById<MaterialTextView>(R.id.tvPrompt)
        val btnMerge = view.findViewById<MaterialButton>(R.id.btnMerge)

        if (FilesFragment.selectedFiles.size == 2) {
            btnMerge.visibility = View.VISIBLE
            btnMerge.setOnClickListener {
                mergeAudioFiles(FilesFragment.selectedFiles[0], FilesFragment.selectedFiles[1])
            }
        } else {
            tvPrompt.visibility = View.VISIBLE
        }
    }

    private fun showProgressDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(R.layout.progress_dialog)
        builder.setCancelable(false)
        progressDialog = builder.create()
        progressDialog?.show()
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
    }

    private fun mergeAudioFiles(file1: String, file2: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val mergedFilesDir = File(downloadsDir, "Merged Files")

        if (!mergedFilesDir.exists()) {
            mergedFilesDir.mkdirs()
        }

        val outputFile = File(mergedFilesDir, "merged_output_${System.currentTimeMillis()}.mp3")

        val command = "-i \"${downloadsDir}/SoundMixer/$file1\" -i \"${downloadsDir}/SoundMixer/$file2\" " +
                "-filter_complex amix=inputs=2:duration=first:dropout_transition=2 " +
                "-c:a libmp3lame -q:a 4 \"${outputFile.absolutePath}\""

        showProgressDialog()
        FFmpegKit.executeAsync(command) { session ->
            val returnCode = session.returnCode
            requireActivity().runOnUiThread {
                dismissProgressDialog()

                if (ReturnCode.isSuccess(returnCode)) {
                    Toast.makeText(
                        requireContext(),
                        "Files merged successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to merge files", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

}

