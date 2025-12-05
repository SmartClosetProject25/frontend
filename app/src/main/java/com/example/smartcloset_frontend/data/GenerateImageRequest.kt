package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

@Serializable
data class GenerateImageRequest(
    val item_ids: List<String>
)
