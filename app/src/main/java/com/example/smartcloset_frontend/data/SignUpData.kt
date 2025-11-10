package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

@Serializable
data class SignUpData(
    val email: String,
    val password: String,
    val userImageUrl: String,
)