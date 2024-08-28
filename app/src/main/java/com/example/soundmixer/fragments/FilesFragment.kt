package com.example.soundmixer.fragments

import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundmixer.R
import com.example.soundmixer.adapter.FilesAdapter
import java.io.File

class FilesFragment : Fragment() {

    companion object {
        var selectedFiles: MutableList<String> = mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_files, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewFiles)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val soundMixerDir = File(downloadsDir, "SoundMixer")

        val fileNames = soundMixerDir.listFiles()?.map { it.name } ?: emptyList()

        val adapter = FilesAdapter(fileNames) { selectedFile ->
            if (selectedFiles.contains(selectedFile)) {
                selectedFiles.remove(selectedFile)
            } else {
                if (selectedFiles.size < 2) {
                    selectedFiles.add(selectedFile)
                } else {
                    Toast.makeText(requireContext(), "You can select only two files", Toast.LENGTH_SHORT).show()
                }
            }
        }
        recyclerView.adapter = adapter
    }
}

