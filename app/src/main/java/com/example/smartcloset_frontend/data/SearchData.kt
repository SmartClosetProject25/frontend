package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

@Serializable
data class SearchData(
    val searchValue: String,
)