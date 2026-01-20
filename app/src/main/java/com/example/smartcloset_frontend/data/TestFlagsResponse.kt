package com.example.smartcloset_frontend.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TestFlagsResponse(
    @SerialName("enable_ai_image")
    val enableAiImage: Boolean = true,
    @SerialName("enable_ai_suggest")
    val enableAiSuggest: Boolean = true
)

