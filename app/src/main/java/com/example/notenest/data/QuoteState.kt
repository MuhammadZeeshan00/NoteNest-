package com.example.notenest.data

sealed class QuoteState {
    object Loading : QuoteState()
    data class Success(val quotes: List<Quote>) : QuoteState()
    data class Error(val message: String) : QuoteState()
}