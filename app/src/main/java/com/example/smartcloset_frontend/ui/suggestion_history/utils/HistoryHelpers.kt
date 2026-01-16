package com.example.smartcloset_frontend.ui.suggestion_history.utils

import android.util.Log
import androidx.navigation.NavHostController
import com.example.smartcloset_frontend.data.CoordinateData
import com.example.smartcloset_frontend.data.CoordinateItem
import com.example.smartcloset_frontend.data.Item
import com.example.smartcloset_frontend.data.Items
import com.example.smartcloset_frontend.data.Proposal
import com.example.smartcloset_frontend.ui.suggestion_history.HistoryCoordinate
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel

/**
 * 履歴画面用のヘルパー関数
 */

/**
 * タグを構築する
 */
fun buildTags(coordinate: CoordinateData): List<String> {
    val tags = mutableListOf<String>()

    // sceneを追加
    if (coordinate.scene.isNotBlank()) {
        tags.add(coordinate.scene)
    }

    // featuresからタグを追加
    coordinate.features.forEach { (key, value) ->
        if (value.isNotBlank()) {
            when (key) {
                "style" -> tags.add(value)
                "season" -> tags.addAll(value.split(",").map { it.trim() }.filter { it.isNotBlank() })
                "color_scheme" -> tags.add(value)
            }
        }
    }

    return tags.take(5) // 最大5つまで
}

/**
 * CoordinateItemからItemに変換
 */
fun coordinateItemToItem(coordinateItem: CoordinateItem): Item {
    return Item(
        id = coordinateItem.id,
        item_name = coordinateItem.name,
        image_path = coordinateItem.image_path,
        taste = emptyList() // CoordinateItemにはtasteがないため空リスト
    )
}

/**
 * CoordinateDataからProposalを作成
 */
fun createProposalFromCoordinateData(coordinate: CoordinateData): Proposal {
    val items = Items(
        tops = coordinateItemToItem(coordinate.top),
        bottoms = coordinateItemToItem(coordinate.bottom),
        outer = coordinate.outer?.let { coordinateItemToItem(it) }
    )

    val itemIds = mutableListOf<Int>()
    itemIds.add(coordinate.top.id)
    itemIds.add(coordinate.bottom.id)
    coordinate.outer?.let { itemIds.add(it.id) }

    // reasonはfeaturesから生成（簡易版）
    val reason = buildString {
        if (coordinate.scene.isNotBlank()) {
            append("シーン: ${coordinate.scene}")
        }
        coordinate.features["style"]?.let {
            if (isNotEmpty()) append("、")
            append("スタイル: $it")
        }
    }.ifBlank { "コーディネート" }

    return Proposal(
        pattern = 0, // パターンは履歴には保存されていないため0
        items = items,
        item_ids = itemIds,
        reason = reason,
        coordinate_id = coordinate.coordinate_id
    )
}

/**
 * 生成結果画面に移動
 */
fun navigateToGeneratedResult(
    suggestion: HistoryCoordinate,
    navController: NavHostController,
    suggestionViewModel: SuggestionViewModel,
    coordinateDataMap: Map<Int, CoordinateData>
) {
    val coordinateId = suggestion.id.toIntOrNull()
    val coordinateData = coordinateId?.let { coordinateDataMap[it] }

    if (coordinateData != null) {
        // Proposalを作成
        val proposal = createProposalFromCoordinateData(coordinateData)

        // 生成画像のパスを設定
        val generatedImagePath = suggestion.genimgPath

        // SuggestionViewModelにデータを設定
        suggestionViewModel.setHistoryData(
            proposal = proposal,
            coordinateId = coordinateId,
            generatedImagePath = generatedImagePath
        )

        // ナビゲーション
        navController.navigate("generate")
    } else {
        Log.e("HistoryHelpers", "Coordinate data not found for id: ${suggestion.id}")
    }
}