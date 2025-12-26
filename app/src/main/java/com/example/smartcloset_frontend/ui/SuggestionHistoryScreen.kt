package com.example.smartcloset_frontend.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.data.CoordinateData
import com.example.smartcloset_frontend.data.CoordinateItem
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

    // 日付別にグループ化（検索クエリでフィルタリング）
    val historyData = remember(coordinatesData, searchQuery) {
        val filteredCoordinates = coordinatesData?.coordinates?.filter { coordinate ->
            // 検索クエリが空の場合は全件表示
            if (searchQuery.isBlank()) {
                true
            } else {
                // アイテム名で検索（大文字小文字を区別しない）
                val query = searchQuery.lowercase()
                coordinate.top.name.lowercase().contains(query) ||
                coordinate.bottom.name.lowercase().contains(query) ||
                coordinate.scene.lowercase().contains(query) ||
                coordinate.features.values.any { it.lowercase().contains(query) }
            }
        }
        
        filteredCoordinates?.groupBy { coordinate ->
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
                        outer = null, // 将来的にAPIレスポンスにouterが追加された場合に対応
                        top = coordinate.top,
                        bottom = coordinate.bottom,
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
                        placeholder = { Text("アイテム名で検索", color = Color.Gray) },
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
    Card(
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // アウター（ある場合）
            suggestion.outer?.let { outer ->
                HistoryItemDisplay(
                    item = outer,
                    label = "アウター"
                )
            }
            
            // トップス
            HistoryItemDisplay(
                item = suggestion.top,
                label = "トップス"
            )
            
            // ボトムス
            HistoryItemDisplay(
                item = suggestion.bottom,
                label = "ボトムス"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
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

@Composable
fun HistoryItemDisplay(item: CoordinateItem, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 画像
        Box(
            modifier = Modifier
                .size(width = 70.dp, height = 90.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE0E0E0))
                .border(1.dp, Color(0xFFC0C0C0), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            val imageUrl = buildImageUrl(item.image_path)
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(label, color = Color.Gray, fontSize = 10.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        // テキスト
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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
    val outer: CoordinateItem?,
    val top: CoordinateItem,
    val bottom: CoordinateItem,
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
