package com.example.smartcloset_frontend.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionHistoryScreen(navController: NavHostController) {

    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // 履歴画面は「1枚画像＋タグ」の簡易モデルを使う
    val historyData = remember {
        listOf(
            HistoryDateGroup(
                date = "2025/10/10",
                suggestions = listOf(
                    HistoryCoordinate(
                        id = "1",
                        imageUrl = null,
                        tags = listOf("アウター", "ブルゾン", "グレイ")
                    ),
                    HistoryCoordinate(
                        id = "2",
                        imageUrl = null,
                        tags = listOf("オフホワイト", "ワイドパンツ")
                    ),
                    HistoryCoordinate(
                        id = "3",
                        imageUrl = null,
                        tags = listOf("ジャケット", "カジュアル")
                    )
                )
            ),
            HistoryDateGroup(
                date = "2025/10/9",
                suggestions = listOf(
                    HistoryCoordinate("4", null, listOf("ブルゾン", "カーキ")),
                    HistoryCoordinate("5", null, listOf("シャツ", "白")),
                    HistoryCoordinate("6", null, listOf("ジャケット", "ブラック"))
                )
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column {

            // ──────────────────
            //   ヘッダー
            // ──────────────────
            TopAppBar(
                title = {
                    Text(
                        text = "提案履歴",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る", tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchVisible = !isSearchVisible
                        if (!isSearchVisible) searchQuery = ""
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "検索", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )

            // ──────────────────
            //   検索フィールド
            // ──────────────────
            if (isSearchVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("検索", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        isSearchVisible = false
                        searchQuery = ""
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.Black)
                    }
                }
            }

            // ──────────────────
            //   日別一覧
            // ──────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                historyData.forEach { dateGroup ->
                    DateGroupSection(dateGroup)
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun DateGroupSection(dateGroup: HistoryDateGroup) {
    Column {

        // 日付ヘッダー
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateGroup.date,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(8.dp))

            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )

            Spacer(Modifier.width(8.dp))

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // 横スクロールカード
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(dateGroup.suggestions) { suggestion ->
                HistoryCoordinateCard(suggestion)
            }
        }
    }
}

@Composable
fun HistoryCoordinateCard(suggestion: HistoryCoordinate) {

    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Box {

            // 画像
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (suggestion.imageUrl != null) {
                    AsyncImage(
                        model = suggestion.imageUrl,
                        contentDescription = "履歴コーデ",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("画像", color = Color.Gray, fontSize = 14.sp)
                }

                // タグ
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    suggestion.tags.take(3).forEach {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .background(Color(0xFFE0F7FA), RoundedCornerShape(4.dp))
                                .padding(6.dp, 2.dp)
                        ) {
                            Text(text = it, color = Color(0xFF2196F3), fontSize = 10.sp)
                        }
                    }
                }
            }

            // いいね/よくない
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = {
                    isLiked = !isLiked
                    if (isLiked) isDisliked = false
                }) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = "いいね",
                        tint = if (isLiked) Color(0xFF2196F3) else Color.White
                    )
                }

                IconButton(onClick = {
                    isDisliked = !isDisliked
                    if (isDisliked) isLiked = false
                }) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = "よくない",
                        tint = if (isDisliked) Color(0xFFE53935) else Color.White,
                        modifier = Modifier.rotate(180f)
                    )
                }
            }
        }
    }
}

///// データクラス（履歴専用）
data class HistoryDateGroup(
    val date: String,
    val suggestions: List<HistoryCoordinate>
)

data class HistoryCoordinate(
    val id: String,
    val imageUrl: String?,
    val tags: List<String>
)
