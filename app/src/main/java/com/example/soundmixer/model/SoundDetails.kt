package com.example.soundmixer.model

import com.google.gson.annotations.SerializedName

data class FreesoundResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Sound>
)

data class Sound(
    val id: Int,
    val name: String,
    val previews: PreviewURLs?
)

/*data class Previews(
    val preview_hq_mp3: String,
    val preview_hq_ogg: String,
    val preview_lq_mp3: String,
    val preview_lq_ogg: String
)*/


data class SoundDetails(
    val id: Int,
    val name: String,
    val previews: PreviewURLs
)

data class PreviewURLs(
    @SerializedName("preview-hq-mp3") val previewHqMp3: String,
    @SerializedName("preview-hq-ogg") val previewHqOgg: String,
    @SerializedName("preview-lq-mp3") val previewLqMp3: String,
    @SerializedName("preview-lq-ogg") val previewLqOgg: String
)

