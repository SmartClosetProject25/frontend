package com.example.smartcloset_frontend.data.repository

import com.example.smartcloset_frontend.data.LoginData
import com.example.smartcloset_frontend.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class AuthRepository {
    suspend fun signup(
        email: String,
        password: String,
        imagePart: MultipartBody.Part
    ) = RetrofitClient.instance.signup(
        email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
        password = password.toRequestBody("text/plain".toMediaTypeOrNull()),
        image = imagePart
    )

    suspend fun login(loginData: LoginData) =
        RetrofitClient.instance.login(loginData)
}