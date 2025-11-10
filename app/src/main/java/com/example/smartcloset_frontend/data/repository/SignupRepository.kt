package com.example.smartcloset_frontend.data.repository

import com.example.smartcloset_frontend.data.SignUpData
import com.example.smartcloset_frontend.network.RetrofitClient

class SignupRepository {
    suspend fun signup(signupData: SignUpData) =
        RetrofitClient.instance.signup(signupData)
}