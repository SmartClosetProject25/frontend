package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetConfirmData(
    val token: String,
    val new_password: String
)

