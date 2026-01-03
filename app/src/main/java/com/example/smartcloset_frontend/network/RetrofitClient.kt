package com.example.smartcloset_frontend.network

import com.example.smartcloset_frontend.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

// Retrofitのインスタンスをシングルトン（アプリ内で唯一の存在）として生成するためのオブジェクト
object RetrofitClient {
    // 接続先のベースURL。AndroidエミュレータからPCのローカルホストにアクセスする場合、'10.0.2.2' を使用する
    private const val BASE_URL = BuildConfig.SERVER_URL // ご自身のFlaskサーバーのアドレスに置き換えてください

    // JSONのパーサー設定。サーバーからのレスポンスに未知のキーが含まれていてもエラーにしない
    private val json = Json {
        ignoreUnknownKeys = true
    }

    // CookieJarインスタンス
    private val cookieJar = SimpleCookieJar()

    // タイムアウト設定とCookieJarを追加したOkHttpClientインスタンスを生成
    // 接続タイムアウトは短めに、読み込みタイムアウトはデータ量に応じて長めに設定
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // 接続確立のタイムアウト
        .readTimeout(120, TimeUnit.SECONDS) // データ読み込みのタイムアウト（大量データ対応）
        .writeTimeout(30, TimeUnit.SECONDS) // データ送信のタイムアウト
        .cookieJar(cookieJar) // Cookie管理を追加
        .addInterceptor(BaseUrlOverrideInterceptor())
        .build()

    // ApiServiceのインスタンスを遅延初期化で生成する
    // 'lazy' を使うことで、実際に 'instance' が初めて呼び出されたときに一度だけ初期化処理が走る
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL) // どのサーバーに接続するか
            .client(okHttpClient) // カスタムしたOkHttpClientを設定
            // Kotlinx SerializationをRetrofitのコンバーターとして使用するための設定
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        // 上記設定を基に、ApiServiceインターフェースの実装を生成
        retrofit.create(ApiService::class.java)
    }

    // Cookieをクリアする関数（ログアウト時などに使用）
    fun clearCookies() {
        cookieJar.clearCookies()
    }
}
