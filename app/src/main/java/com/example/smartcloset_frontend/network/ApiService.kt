package com.example.smartcloset_frontend.network

import com.example.smartcloset_frontend.data.LoginData
import com.example.smartcloset_frontend.data.ProfileData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// RetrofitでサーバーAPIと通信するためのインターフェース
interface ApiService {

    // プロフィール情報をサーバーに送信（更新）するためのPOSTリクエストを定義
    @POST("/update_profile") // サーバーのエンドポイント（URLのパス部分）を指定
    suspend fun updateProfile(
        @Body profileData: ProfileData // リクエストのボディに含めるデータ
    ): Response<Unit> // サーバーからのレスポンス。今回はボディがないためUnitを指定

    //login処理
    @POST("/login")
    suspend fun login(
        @Body loginData: LoginData
    ): Response<Unit>

    //signup処理
    @POST("/signup")
    suspend fun signup(
        @Body loginData: LoginData
    ): Response<Unit>


}
