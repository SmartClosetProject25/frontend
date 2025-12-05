package com.example.smartcloset_frontend.network

import com.example.smartcloset_frontend.data.LoginData
import com.example.smartcloset_frontend.data.ProfileData
import com.example.smartcloset_frontend.data.SignUpData
import com.example.smartcloset_frontend.data.SearchData
import com.example.smartcloset_frontend.data.ItemData
import com.example.smartcloset_frontend.data.GenerateOutfitData
import com.example.smartcloset_frontend.data.GenerateOutfitWithWeather
import com.example.smartcloset_frontend.data.GetItemsResponse
import com.example.smartcloset_frontend.data.ItemDetailData
import com.example.smartcloset_frontend.data.JudgeRequestData
import com.example.smartcloset_frontend.data.LocationData
import com.example.smartcloset_frontend.data.ProposalResponse
import com.example.smartcloset_frontend.data.TodayPlanData
import com.example.smartcloset_frontend.data.WeatherData
import com.example.smartcloset_frontend.data.PasswordResetRequestData
import com.example.smartcloset_frontend.data.PasswordResetConfirmData

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

// RetrofitでサーバーAPIと通信するためのインターフェース
interface ApiService {
    // プロフィール更新
    @POST("/update_profile")
    suspend fun updateProfile(
        @Body profileData: ProfileData
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
    @GET("/search")
    suspend fun search(
        @Body searchData: SearchData
    ): Response<Unit>
//    ): Response<SearchResponse> レスポンスがある場合はこれにする　Dataの定義も必要

//    アイテム取得処理
    @GET("/get_item")
    suspend fun getItems(
        @Query("userId") userId: Int
    ): GetItemsResponse

    // アイテム詳細取得処理
    @GET("/get_item_detail")
    suspend fun getItemDetail(
        @Query("itemId") itemId: Int
    ): ItemDetailData

    @Multipart
    @POST("/add_item")
    suspend fun addItem(
        @Part("userId") userId: RequestBody,
        @Part("itemName") itemName: RequestBody,
        @Part("color") color: RequestBody,
        @Part("pattern") pattern: RequestBody,
        @Part("size") size: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("category") category: RequestBody,
        @Part("material") material: RequestBody,
        @Part("feature") feature: RequestBody,
        @Part("season") season: RequestBody,
        @Part("taste") taste: RequestBody,
        @Part image: MultipartBody.Part,
    ): Response<Unit>

    @POST("/judgement")
    suspend fun judgement(
        @Body body: JudgeRequestData
    ): Response<Unit>

    @POST("/generate_outfit")
    suspend fun generateOutfit(
        @Body data: GenerateOutfitData
    ): Response<ResponseBody>

    @POST("/generate_outfit_with_weather")
    suspend fun generateOutfitWithWeather(
        @Body data: GenerateOutfitWithWeather
    ): Response<ResponseBody>

    @POST("/get_weather")
    suspend fun getWeather(
        @Body locationData:LocationData
    ): Response<WeatherData>

    // パスワードリセットリクエスト（メール送信）
    @POST("/auth/password-reset/request")
    suspend fun requestPasswordReset(
        @Body requestData: PasswordResetRequestData
    ): Response<Unit>

    // パスワードリセット実行
    @POST("/auth/password-reset/confirm")
    suspend fun confirmPasswordReset(
        @Body confirmData: PasswordResetConfirmData
    ): Response<Unit>

    @POST("/send_today_plan")
    suspend fun sendTodayPlan(@Body todayPlanData: TodayPlanData): Response<ProposalResponse>



}
