package com.example.smartcloset_frontend.data.repository
import com.example.smartcloset_frontend.network.RetrofitClient
import com.example.smartcloset_frontend.data.LoginData
class LoginRepository {
    suspend fun login(loginData: LoginData) =
        RetrofitClient.instance.login(loginData)
}