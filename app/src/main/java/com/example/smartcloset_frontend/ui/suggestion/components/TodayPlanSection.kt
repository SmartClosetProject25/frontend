package com.example.smartcloset_frontend.ui.suggestion.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 今日の予定入力セクション
 */
@Composable
fun TodayPlanSection(
    todayPlan: String,
    onPlanChange: (String) -> Unit,
    isSending: Boolean,
    onSendClick: () -> Unit,
    gender: String? = null,
    onGenderChange: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "今日の予定",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = todayPlan,
                onValueChange = onPlanChange,
                placeholder = { Text("ランチ", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f),
                singleLine = true,
                enabled = !isSending
            )

            // 男女選択のFilterChip
            Row(
                modifier = Modifier.height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 男性ボタン
                FilterChip(
                    selected = gender == "male",
                    onClick = {
                        onGenderChange(if (gender == "male") null else "male")
                    },
                    enabled = !isSending,
                    label = { Text("男", fontSize = 14.sp) },
                    modifier = Modifier.height(36.dp)
                )

                // 女性ボタン
                FilterChip(
                    selected = gender == "female",
                    onClick = {
                        onGenderChange(if (gender == "female") null else "female")
                    },
                    enabled = !isSending,
                    label = { Text("女", fontSize = 14.sp) },
                    modifier = Modifier.height(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 予定の提案ボタン
        val planSuggestions = listOf(
            "表参道でおしゃれにカフェデート",
            "1日中歩き回るテーマパーク",
            "下北沢で古着屋巡り",
            "静かな美術館でアート鑑賞",
            "寒さに負けない初詣",
            "友達と賑やかに新年会",
            "銀座で少し贅沢なランチ",
            "清潔感重視の大事なプレゼン",
            "落ち着いた雰囲気の結婚式二次会"
        )

        // ランダムに4つ選んで表示
        val randomSuggestions = remember {
            planSuggestions.shuffled().take(4)
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(randomSuggestions) { suggestion ->
                OutlinedButton(
                    onClick = { onPlanChange(suggestion) },
                    enabled = !isSending,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSendClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            enabled = !isSending
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("送信", color = Color.White)
            }
        }
    }
}