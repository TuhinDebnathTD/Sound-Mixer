package com.example.soundmixer.retrofit

import com.example.soundmixer.model.FreesoundResponse
import com.example.soundmixer.model.SoundDetails
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FreesoundApiService {
    @GET("search/text/")
    fun searchSounds(
        @Query("query") query: String,
        @Query("token") token: String = "76mpGGSskS53cg1RowRPA1lt4yTEzWRFMUTGB4S0",
        @Query("fields") fields: String = "id,name"
    ): Call<FreesoundResponse>

    @GET("sounds/{id}/")
    fun getSoundDetails(
        @Path("id") id: Int,
        @Query("token") token: String = "76mpGGSskS53cg1RowRPA1lt4yTEzWRFMUTGB4S0",
        @Query("fields") fields: String = "id,name,previews"
    ): Call<SoundDetails>
}
