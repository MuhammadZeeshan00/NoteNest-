package com.example.notenest.data

import com.google.gson.annotations.SerializedName

data class Quote(
    @SerializedName("id")
    val id: Int,
    @SerializedName("text")
    val text: String,
    @SerializedName("author")
    val author: String
)