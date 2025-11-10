package com.example.smartcloset_frontend.data.repository

import com.example.smartcloset_frontend.data.LoginData
import com.example.smartcloset_frontend.network.RetrofitClient

class SignupRepository {
    suspend fun signup(signupData: LoginData) =
        RetrofitClient.instance.signup(signupData)
}