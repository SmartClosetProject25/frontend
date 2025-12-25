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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.data.CoordinateData
import com.example.smartcloset_frontend.viewmodel.SuggestionHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionHistoryScreen(
    navController: NavHostController,
    viewModel: SuggestionHistoryViewModel
) {

    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val coordinatesData by viewModel.coordinatesData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // 初回ロード時にデータを取得
    LaunchedEffect(Unit) {
        viewModel.fetchCoordinates()
    }

    // 日付別にグループ化
    val historyData = remember(coordinatesData) {
        coordinatesData?.coordinates?.groupBy { coordinate ->
            // "2025-12-01 00:00:00" -> "2025/12/01" に変換
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                val date = inputFormat.parse(coordinate.created_at)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                coordinate.created_at.substring(0, 10).replace("-", "/")
            }
        }?.map { (date, coordinates) ->
            HistoryDateGroup(
                date = date,
                suggestions = coordinates.map { coordinate ->
                    HistoryCoordinate(
                        id = coordinate.coordinate_id.toString(),
                        imageUrl = buildImageUrl(coordinate.top.image_path),
                        tags = buildTags(coordinate)
                    )
                }
            )
        }?.sortedByDescending { it.date } ?: emptyList()
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
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = error ?: "エラーが発生しました",
                                color = Color.Red
                            )
                        }
                    }
                    historyData.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "履歴がありません",
                                color = Color.Gray
                            )
                        }
                    }
                    else -> {
                        historyData.forEach { dateGroup ->
                            DateGroupSection(dateGroup)
                            Spacer(Modifier.height(24.dp))
                        }
                    }
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
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
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

// ヘルパー関数: 画像URLを構築
private fun buildImageUrl(imagePath: String): String? {
    if (imagePath.isBlank()) return null
    return if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
        imagePath
    } else {
        val baseUrl = BuildConfig.SERVER_URL.trimEnd('/')
        val path = if (imagePath.startsWith("/")) imagePath else "/$imagePath"
        "$baseUrl$path"
    }
}

// ヘルパー関数: タグを構築
private fun buildTags(coordinate: CoordinateData): List<String> {
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
