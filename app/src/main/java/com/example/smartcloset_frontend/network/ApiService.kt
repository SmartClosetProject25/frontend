package com.example.smartcloset_frontend.network

import com.example.smartcloset_frontend.data.LoginData
import com.example.smartcloset_frontend.data.ProfileData
import com.example.smartcloset_frontend.data.SignUpData
import com.example.smartcloset_frontend.data.SearchData
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
        @Body signUpData: SignUpData
    ): Response<Unit>
//    検索処理
    @POST("/search")
    suspend fun search(
        @Body searchData: SearchData
    ): Response<Unit>
//    ): Response<SearchResponse> レスポンスがある場合はこれにする　Dataの定義も必要

}
