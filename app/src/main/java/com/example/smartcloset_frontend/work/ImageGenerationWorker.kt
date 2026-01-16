package com.example.smartcloset_frontend.work

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smartcloset_frontend.data.repository.TodayPlanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageGenerationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = TodayPlanRepository()
    private val sharedPreferences: SharedPreferences =
        context.applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 入力データを取得
            val imagePaths = inputData.getStringArray(KEY_IMAGE_PATHS)?.toList()
                ?: return@withContext Result.failure()
            
            val modelTemplate = inputData.getString(KEY_MODEL_TEMPLATE)
            val coordinateId = inputData.getInt(KEY_COORDINATE_ID, -1).takeIf { it != -1 }
            val workRequestId = inputData.getString(KEY_WORK_REQUEST_ID) ?: ""
            val hasModelImage = inputData.getBoolean(KEY_HAS_MODEL_IMAGE, false)
            val isOuter = inputData.getBoolean(KEY_IS_OUTER, true)  // デフォルトはtrue
            
            // SharedPreferencesからBase64データを取得
            val modelImageBase64 = if (hasModelImage) {
                val key = "model_image_base64_$workRequestId"
                val base64 = sharedPreferences.getString(key, null)
                // 取得後に削除（メモリ節約のため）
                base64?.let {
                    sharedPreferences.edit().remove(key).apply()
                }
                base64
            } else {
                null
            }

            Log.d("ImageGenerationWorker", "画像生成を開始: imagePaths=$imagePaths, coordinateId=$coordinateId, hasModelImage=$hasModelImage, isOuter=$isOuter")

            // 画像生成を実行
            val response = repository.generateImage(
                imagePaths = imagePaths,
                modelImageBase64 = modelImageBase64,
                modelTemplate = modelTemplate,
                coordinateId = coordinateId,
                isOuter = isOuter
            )

            if (response.isSuccessful) {
                val imageResponse = response.body()
                Log.d("ImageGenerationWorker", "レスポンス受信: status=${imageResponse?.status}, image_url_full=${imageResponse?.image_url_full}, image_url=${imageResponse?.image_url}, image=${imageResponse?.image}")
                
                if (imageResponse?.status == "success") {
                    val imageUrl = imageResponse.image_url_full
                        ?: imageResponse.image_url
                        ?: imageResponse.image

                    if (imageUrl != null) {
                        Log.d("ImageGenerationWorker", "画像生成成功: $imageUrl")
                        
                        // 生成完了情報をSharedPreferencesに保存（トースト表示用）
                        sharedPreferences.edit().apply {
                            putBoolean("image_generation_complete", true)
                            putString("generated_image_url", imageUrl)
                            coordinateId?.let { putInt("generated_coordinate_id", it) }
                            putBoolean("has_new_generated_image", true) // バッジ表示用
                            putBoolean("is_generating_image", false) // 生成中フラグを解除
                            apply()
                        }
                        
                        Log.d("ImageGenerationWorker", "生成完了情報を保存しました")
                        return@withContext Result.success()
                    } else {
                        Log.e("ImageGenerationWorker", "画像URLが取得できませんでした")
                        sharedPreferences.edit().putBoolean("is_generating_image", false).apply()
                        return@withContext Result.failure()
                    }
                } else {
                    val errorMessage = imageResponse?.message ?: "画像生成に失敗しました"
                    Log.e("ImageGenerationWorker", "画像生成失敗: $errorMessage")
                    sharedPreferences.edit().putBoolean("is_generating_image", false).apply()
                    return@withContext Result.failure()
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ImageGenerationWorker", "画像生成エラー: code=${response.code()}, message=${response.message()}, body=$errorBody")
                sharedPreferences.edit().putBoolean("is_generating_image", false).apply()
                return@withContext Result.failure()
            }
        } catch (e: Exception) {
            Log.e("ImageGenerationWorker", "画像生成中に例外が発生", e)
            sharedPreferences.edit().putBoolean("is_generating_image", false).apply()
            return@withContext Result.retry()
        }
    }

    companion object {
        const val KEY_IMAGE_PATHS = "image_paths"
        const val KEY_MODEL_IMAGE_BASE64 = "model_image_base64"
        const val KEY_MODEL_TEMPLATE = "model_template"
        const val KEY_COORDINATE_ID = "coordinate_id"
        const val KEY_WORK_REQUEST_ID = "work_request_id"
        const val KEY_HAS_MODEL_IMAGE = "has_model_image"
        const val KEY_IS_OUTER = "is_outer"
    }
}
