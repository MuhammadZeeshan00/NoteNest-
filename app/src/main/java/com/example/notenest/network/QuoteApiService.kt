package com.example.notenest.network

import android.text.style.QuoteSpan
import com.example.notenest.data.Quote
import retrofit2.http.GET

interface QuoteApiService {
    @GET("quotes")
    suspend fun getQuotes(): List<Quote>
}