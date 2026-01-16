package com.example.smartcloset_frontend.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.smartcloset_frontend.data.Proposal
import com.example.smartcloset_frontend.utils.ImageUtils
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel

/**
 * 画像生成処理を共通化するヘルパーオブジェクト
 */
object ImageGenerationHelper {
    /**
     * 画像生成を開始する（履歴からも使用可能）
     * @param proposal 提案データ（nullの場合はcoordinateIdから取得を試みる）
     * @param coordinateId コーディネートID（proposalがnullの場合に使用）
     * @param modelBitmap モデル画像（Bitmap、カメラ撮影時）
     * @param modelUri モデル画像（Uri、アルバム選択時）
     * @param modelTemplate モデルテンプレート（"mannequin" または "profile"）
     * @param context Context
     * @param suggestionViewModel SuggestionViewModel
     */
    fun startImageGeneration(
        proposal: Proposal?,
        coordinateId: Int? = null,
        modelBitmap: Bitmap?,
        modelUri: Uri?,
        modelTemplate: String?,
        context: Context,
        suggestionViewModel: SuggestionViewModel
    ) {
        if (proposal == null) {
            Log.e("ImageGenerationHelper", "生成に必要な情報が不足しています: proposal is null")
            return
        }
        
        // proposal.itemsから各アイテムのimage_pathを取得
        val imagePaths = mutableListOf<String>()
        imagePaths.addAll(
            listOfNotNull(
                proposal.items.outer?.image_path,
                proposal.items.tops?.image_path,
                proposal.items.bottoms?.image_path
            )
        )
        
        if (imagePaths.isEmpty()) {
            Toast.makeText(context, "画像パスが取得できませんでした", Toast.LENGTH_SHORT).show()
            Log.e("ImageGenerationHelper", "画像パスが空です")
            return
        }
        
        // モデル画像をbase64エンコード
        val modelImageBase64: String? = when {
            modelBitmap != null -> ImageUtils.bitmapToBase64(modelBitmap)
            modelUri != null -> ImageUtils.uriToBase64(context, modelUri)
            else -> null
        }
        
        val actualCoordinateId = coordinateId ?: proposal.coordinate_id
        Log.d("ImageGenerationHelper", "画像生成を開始: imagePaths=$imagePaths, coordinateId=$actualCoordinateId")
        
        suggestionViewModel.generateImage(
            context = context,
            imagePaths = imagePaths,
            modelImageBase64 = modelImageBase64,
            modelTemplate = modelTemplate,
            proposal = proposal,
            coordinateId = actualCoordinateId,
            useBackgroundGeneration = true
        )
    }
}