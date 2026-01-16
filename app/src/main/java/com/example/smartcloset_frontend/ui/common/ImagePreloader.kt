package com.example.smartcloset_frontend.ui.common

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import coil.imageLoader
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 画像プリロードを管理するオブジェクト
 */
object ImagePreloader {
    /**
     * 複数の画像URLを並列でプリロードする
     * @param context Context
     * @param imageUrls プリロードする画像URLのリスト
     * @return プリロードが完了したかどうか
     */
    suspend fun preloadImages(
        context: Context,
        imageUrls: List<String>
    ): Boolean {
        if (imageUrls.isEmpty()) return true
        
        val imageLoader = context.imageLoader
        
        return try {
            coroutineScope {
                val preloadJobs = imageUrls.map { imageUrl ->
                    async {
                        try {
                            val request = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .build()
                            imageLoader.execute(request)
                        } catch (e: Exception) {
                            Log.e("ImagePreloader", "画像プリロードエラー ($imageUrl): ${e.message}", e)
                            // 個別のエラーは無視して続行
                        }
                    }
                }
                // すべてのプリロードが完了するまで待機
                preloadJobs.awaitAll()
            }
            true
        } catch (e: Exception) {
            Log.e("ImagePreloader", "画像プリロードエラー: ${e.message}", e)
            false
        }
    }
}