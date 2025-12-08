package com.example.smartcloset_frontend.data.repository

import com.example.smartcloset_frontend.data.LoginData
import com.example.smartcloset_frontend.data.SignUpData
import com.example.smartcloset_frontend.network.RetrofitClient

class AuthRepository {
    suspend fun signup(signupData: SignUpData) =
        RetrofitClient.instance.signup(signupData)

    suspend fun login(loginData: LoginData) =
        RetrofitClient.instance.login(loginData)

}