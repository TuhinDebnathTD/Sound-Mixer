package com.example.soundmixer.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://freesound.org/apiv2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: FreesoundApiService by lazy {
        retrofit.create(FreesoundApiService::class.java)
    }
}