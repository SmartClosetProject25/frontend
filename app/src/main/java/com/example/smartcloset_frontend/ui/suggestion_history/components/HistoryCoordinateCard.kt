package com.example.smartcloset_frontend.ui.suggestion_history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcloset_frontend.ui.suggestion_history.HistoryCoordinate

/**
 * 履歴コーディネートカードコンポーネント
 */
@Composable
fun HistoryCoordinateCard(
    suggestion: HistoryCoordinate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 2x2グリッドレイアウト
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1行目
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // アウター（ある場合）または空きスペース
                    if (suggestion.outer != null) {
                        HistoryItemGridCell(
                            item = suggestion.outer,
                            label = "アウター",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // トップス
                    HistoryItemGridCell(
                        item = suggestion.top,
                        label = "トップス",
                        modifier = Modifier.weight(1f)
                    )
                }

                // 2行目
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ボトムス
                    HistoryItemGridCell(
                        item = suggestion.bottom,
                        label = "ボトムス",
                        modifier = Modifier.weight(1f)
                    )

                    // 4枚目: 生成画像
                    if (suggestion.genimgPath != null && suggestion.genimgPath.isNotBlank()) {
                        HistoryGeneratedImageCell(
                            imagePath = suggestion.genimgPath,
                            label = "生成画像",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // タグ
            if (suggestion.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    suggestion.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE0F7FA), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                color = Color(0xFF2196F3),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}