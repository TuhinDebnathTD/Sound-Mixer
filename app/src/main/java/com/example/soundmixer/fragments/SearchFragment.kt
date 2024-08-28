package com.example.soundmixer.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundmixer.R
import com.example.soundmixer.adapter.SongsAdapter
import com.example.soundmixer.model.FreesoundResponse
import com.example.soundmixer.model.SoundDetails
import com.example.soundmixer.retrofit.RetrofitInstance
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private lateinit var searchBar: SearchBar
    private lateinit var searchView: SearchView

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongsAdapter

    private var processingDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchBar = view.findViewById(R.id.searchBar)
        searchView = view.findViewById(R.id.searchView)

        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchBar.setOnClickListener {
            searchView.show()
        }

        searchView.editText.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(textView.windowToken, 0)
                showProcessingDialog()

                val query = textView.text.toString()
                //performSearch(query)
                //fetchSounds(query)
                CoroutineScope(Dispatchers.IO).launch {
                    fetchSounds(query)
                }
                true
            } else {
                false
            }
        }

        return view
    }

    private fun performSearch(query: String) {
        Toast.makeText(context, "Searching for: $query", Toast.LENGTH_SHORT).show()
    }

    /*private fun fetchSongs(query: String) {
        RetrofitInstance.api.searchSongs(query).enqueue(object : Callback<FreesoundResponse> {
            override fun onResponse(call: Call<FreesoundResponse>, response: Response<FreesoundResponse>) {
                response.body()?.let {
                    Log.d("API_RESPONSE", response.body()?.toString() ?: "null body")
                    adapter = SongsAdapter(it.results)
                    recyclerView.adapter = adapter
                    Toast.makeText(context, "Searching for: $query", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FreesoundResponse>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }*/

    private suspend fun fetchSounds(query: String) {
        RetrofitInstance.api.searchSounds(query).enqueue(object : Callback<FreesoundResponse> {
            override fun onResponse(call: Call<FreesoundResponse>, response: Response<FreesoundResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { freesoundResponse ->
                        Log.d("API_RESPONSE", response.body()?.toString() ?: "null body")
                        val soundIds = freesoundResponse.results.map { it.id }
                        CoroutineScope(Dispatchers.IO).launch {
                            fetchSoundDetails(soundIds)
                        }
                    }
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FreesoundResponse>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private suspend fun fetchSoundDetails(soundIds: List<Int>) {
        val soundDetailsList = mutableListOf<SoundDetails>()
        var fetchedCount = 0

        for (id in soundIds) {
            RetrofitInstance.api.getSoundDetails(id).enqueue(object : Callback<SoundDetails> {
                override fun onResponse(call: Call<SoundDetails>, response: Response<SoundDetails>) {
                    Log.d("API_RAW_RESPONSE", "Response for sound $id: ${response.raw()}")
                    Log.d("API_BODY", "Body for sound $id: ${response.body()}")

                    if (response.isSuccessful) {
                        response.body()?.let { soundDetails ->
                            soundDetailsList.add(soundDetails)
                            Log.d("API_SOUND_DETAILS", "Sound $id details: $soundDetails")
                        }
                    } else {
                        Log.e("API_ERROR", "Error for sound $id: ${response.code()} - ${response.message()}")
                    }

                    fetchedCount++
                    if (fetchedCount == soundIds.size) {
                        adapter = SongsAdapter(soundDetailsList)
                        recyclerView.adapter = adapter
                        hideProcessingDialog()
                    }
                }

                override fun onFailure(call: Call<SoundDetails>, t: Throwable) {
                    Log.e("API_FAILURE", "Failed to fetch sound $id: ${t.message}")
                    fetchedCount++
                    if (fetchedCount == soundIds.size) {
                        adapter = SongsAdapter(soundDetailsList)
                        recyclerView.adapter = adapter
                    }
                }
            })
        }
    }
    private fun showProcessingDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_processing, null)

        builder.setView(dialogView)
        builder.setCancelable(false)

        processingDialog = builder.create()
        processingDialog?.show()
    }

    private fun hideProcessingDialog() {
        processingDialog?.dismiss()
    }

}