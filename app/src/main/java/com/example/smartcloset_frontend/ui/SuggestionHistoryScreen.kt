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
    // 検索フィールドの表示状態
    var isSearchVisible by remember { mutableStateOf(false) }
    // 検索クエリ
    var searchQuery by remember { mutableStateOf("") }
    
    // 仮のデータ（後で実際のデータに置き換え）
    val historyData = remember {
        listOf(
            HistoryDateGroup(
                date = "2025/10/10",
                suggestions = listOf(
                    CoordinateSuggestion(
                        id = "1",
                        imageUrl = null,
                        tags = listOf("アウター", "ブルゾン", "オリーウ", "スウェットパンツ", "グレイ")
                    ),
                    CoordinateSuggestion(
                        id = "2",
                        imageUrl = null,
                        tags = listOf("アウター", "ブルゾン", "オリーウ", "ワイドハ", "オフホワイト")
                    ),
                    CoordinateSuggestion(
                        id = "3",
                        imageUrl = null,
                        tags = listOf("アウター", "ブルゾン", "オリーウ", "ワイドハ", "オフホワイト")
                    )
                )
            ),
            HistoryDateGroup(
                date = "2025/10/9",
                suggestions = listOf(
                    CoordinateSuggestion(
                        id = "4",
                        imageUrl = null,
                        tags = listOf("アウター", "ブルゾン", "オリーウ", "スウェットパンツ", "グレイ")
                    ),
                    CoordinateSuggestion(
                        id = "5",
                        imageUrl = null,
                        tags = listOf("アウター", "ブルゾン", "オリーウ", "ワイドハ", "オフホワイト")
                    ),
                    CoordinateSuggestion(
                        id = "6",
                        imageUrl = null,
                        tags = listOf("アウター", "ブルゾン", "オリーウ", "ワイドハ", "オフホワイト")
                    )
                )
            ),
            HistoryDateGroup(
                date = "2025/10/8",
                suggestions = listOf(
                    CoordinateSuggestion(
                        id = "7",
                        imageUrl = null,
                        tags = listOf("アウター", "ブルゾン", "オリーウ", "スウェットパンツ", "グレイ")
                    ),
                    CoordinateSuggestion(
                        id = "8",
                        imageUrl = null,
                        tags = listOf("アウター", "ブルゾン", "オリーウ", "ワイドハ", "オフホワイト")
                    ),
                    CoordinateSuggestion(
                        id = "9",
                        imageUrl = null,
                        tags = listOf("アウター", "ブルゾン", "オリーウ", "ワイドハ", "オフホワイト")
                    )
                )
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // ヘッダー
            TopAppBar(
                title = {
                    Text(
                        text = "提案履歴",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "戻る",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchVisible = !isSearchVisible
                        if (!isSearchVisible) {
                            searchQuery = ""
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "検索",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )

            // 検索フィールド
            if (isSearchVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "検索",
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            isSearchVisible = false
                            searchQuery = ""
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "閉じる",
                            tint = Color.Black
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                historyData.forEach { dateGroup ->
                    DateGroupSection(dateGroup = dateGroup)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun DateGroupSection(dateGroup: HistoryDateGroup) {
    Column {
        // 日付と横線
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateGroup.date,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 横線
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 矢印アイコン
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // コーディネートカードの横スクロールリスト
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(dateGroup.suggestions) { suggestion ->
                HistoryCoordinateCard(suggestion = suggestion)
            }
        }
    }
}

@Composable
fun HistoryCoordinateCard(suggestion: CoordinateSuggestion) {
    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Box {
            // コーディネート画像
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (suggestion.imageUrl != null) {
                    AsyncImage(
                        model = suggestion.imageUrl,
                        contentDescription = "コーディネート",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "画像",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                
                // タグを左上にオーバーレイ
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    suggestion.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .background(
                                    Color(0xFFE0F7FA),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag,
                                color = Color(0xFF2196F3),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
            
            // フィードバックボタンを右下に配置
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // いいねボタン
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        if (isLiked) isDisliked = false
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "いいね",
                        tint = if (isLiked) Color(0xFF2196F3) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // よくないボタン
                IconButton(
                    onClick = {
                        isDisliked = !isDisliked
                        if (isDisliked) isLiked = false
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "よくない",
                        tint = if (isDisliked) Color(0xFFE53935) else Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(180f)
                    )
                }
            }
        }
    }
}

// 仮のデータクラス（後で実際のデータ構造に置き換え）
data class HistoryDateGroup(
    val date: String,
    val suggestions: List<CoordinateSuggestion>
)


