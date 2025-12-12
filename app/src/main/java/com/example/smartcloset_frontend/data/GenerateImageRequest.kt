package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

@Serializable
data class GenerateImageRequest(
    val image_paths: List<String>
)
