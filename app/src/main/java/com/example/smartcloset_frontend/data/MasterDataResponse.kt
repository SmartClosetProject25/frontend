package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

@Serializable
data class MasterDataResponse(
    val status: String,
    val categories: Map<String, String>? = null,
    val sizes: Map<String, String>? = null,
    val colors: Map<String, String>? = null,
    val patterns: Map<String, String>? = null
)

