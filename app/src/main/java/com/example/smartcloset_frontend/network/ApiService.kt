package com.example.smartcloset_frontend.network

import com.example.smartcloset_frontend.data.FavoriteRequest
import com.example.smartcloset_frontend.data.GenerateImageRequest
import com.example.smartcloset_frontend.data.GenerateOutfitData
import com.example.smartcloset_frontend.data.GenerateOutfitWithWeather
import com.example.smartcloset_frontend.data.GetCoordinatesResponse
import com.example.smartcloset_frontend.data.GetItemDetailResponse
import com.example.smartcloset_frontend.data.GetItemsResponse
import com.example.smartcloset_frontend.data.GetProfileResponse
import com.example.smartcloset_frontend.data.ImageResponse
import com.example.smartcloset_frontend.data.JudgeRequestData
import com.example.smartcloset_frontend.data.LocationData
import com.example.smartcloset_frontend.data.LoginData
import com.example.smartcloset_frontend.data.LoginResponse
import com.example.smartcloset_frontend.data.MasterDataResponse
import com.example.smartcloset_frontend.data.PasswordResetConfirmData
import com.example.smartcloset_frontend.data.PasswordResetRequestData
import com.example.smartcloset_frontend.data.ProfileData
import com.example.smartcloset_frontend.data.ProposalResponse
import com.example.smartcloset_frontend.data.SearchData
import com.example.smartcloset_frontend.data.SignUpData
import com.example.smartcloset_frontend.data.TodayPlanData
import com.example.smartcloset_frontend.data.WeatherData
import com.example.smartcloset_frontend.data.WeatherDto
import com.example.smartcloset_frontend.data.TestFlagsResponse

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

    @GET("/get_profile")
    suspend fun getProfile(
        @Query("user_id") userId: Int
    ): GetProfileResponse

    //login処理
    @POST("/login")
    suspend fun login(
        @Body loginData: LoginData
    ): Response<LoginResponse>

    //signup処理（multipart対応）
    @Multipart
    @POST("/signup")
    suspend fun signup(
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part image: MultipartBody.Part
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
    ): GetItemDetailResponse

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

    @Multipart
    @POST("/update_item")
    suspend fun updateItem(
        @Part("itemId") itemId: RequestBody,
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
        @Part image: MultipartBody.Part?,
    ): Response<Unit>


    //組み合わせいいねバッド判定処理
    @POST("/rate_coordinate")
    suspend fun judgement(
        @Body req : JudgeRequestData
    ): Response<Unit>

    // お気に入り設定処理
    @POST("/favorite")
    suspend fun setFavorite(
        @Body request: FavoriteRequest
    )

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
    ): Response<WeatherDto>

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

    @POST("/generate_image")
    suspend fun generateImage(@Body request: GenerateImageRequest): Response<ImageResponse>

    @GET("/get_master_data")
    suspend fun getMasterData(): Response<MasterDataResponse>

    // テストフラグ取得
    @GET("/config/test_flags")
    suspend fun getTestFlags(): Response<TestFlagsResponse>

    // テストフラグ更新（GETクエリで送信）
    @GET("/config/test_flags")
    suspend fun setTestFlags(
        @Query("enable_ai_image") enableAiImage: Boolean,
        @Query("enable_ai_suggest") enableAiSuggest: Boolean
    ): Response<TestFlagsResponse>

    // コーディネート履歴取得処理
    @GET("/get_coordinates")
    suspend fun getCoordinates(): Response<GetCoordinatesResponse>

}
