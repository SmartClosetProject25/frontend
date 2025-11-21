package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequestData(
    val email: String
)

