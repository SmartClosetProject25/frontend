package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LoginData(
    val email: String,
    val password: String
)
@Serializable
data class SignUpData(
    val email: String,
    val password: String,
    val userImageUrl: String,
)

@Serializable
data class LoginResponse(
    val ok: Boolean,
    val message: String? = null,
    @SerialName("user_id")
    val userId: Int,
    val email: String
)