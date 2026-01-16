package com.example.smartcloset_frontend.ui.suggestion_history

import com.example.smartcloset_frontend.data.CoordinateItem

/**
 * 履歴専用のデータクラス
 */
data class HistoryDateGroup(
    val date: String,
    val suggestions: List<HistoryCoordinate>
)

data class HistoryCoordinate(
    val id: String,
    val outer: CoordinateItem?,
    val top: CoordinateItem,
    val bottom: CoordinateItem,
    val genimgPath: String?,
    val tags: List<String>
)