package com.example.soundmixer.fragments

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundmixer.R
import com.example.soundmixer.adapter.PlayFilesAdapter
import java.io.File

class PlayFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_play, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewMergedFiles)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val mergedFilesDir = File(downloadsDir, "Merged Files")

        val mergedFileNames = mergedFilesDir.listFiles()?.map { it.name } ?: emptyList()

        val adapter = PlayFilesAdapter(mergedFileNames, mergedFilesDir)
        recyclerView.adapter = adapter
    }
}
