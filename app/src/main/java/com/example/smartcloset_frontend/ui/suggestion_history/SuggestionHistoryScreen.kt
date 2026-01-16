package com.example.smartcloset_frontend.ui.suggestion_history

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smartcloset_frontend.data.CoordinateData
import com.example.smartcloset_frontend.ui.suggestion_history.components.DateGroupSection
import com.example.smartcloset_frontend.ui.suggestion_history.utils.buildTags
import com.example.smartcloset_frontend.viewmodel.SuggestionHistoryViewModel
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 提案履歴画面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionHistoryScreen(
    navController: NavHostController,
    viewModel: SuggestionHistoryViewModel,
    suggestionViewModel: SuggestionViewModel
) {
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val coordinatesData by viewModel.coordinatesData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // CoordinateDataのマップを作成（coordinate_idをキーとして）
    val coordinateDataMap = remember(coordinatesData) {
        coordinatesData?.coordinates?.associateBy { it.coordinate_id } ?: emptyMap()
    }

    // 画面が表示されるたびにデータを取得
    DisposableEffect(Unit) {
        viewModel.fetchCoordinates()
        onDispose { }
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
                coordinate.outer?.name?.lowercase()?.contains(query) == true ||
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
                        outer = coordinate.outer,
                        top = coordinate.top,
                        bottom = coordinate.bottom,
                        genimgPath = coordinate.genimg_path,
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
            // ヘッダー
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

            // 検索フィールド
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

            // 日別一覧
            val scrollState = rememberScrollState()
            val density = LocalDensity.current
            val screenHeight = with(density) {
                LocalConfiguration.current.screenHeightDp.dp.toPx()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
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
                        // 各日付グループの展開状態を管理（一番新しい日付はデフォルトで展開）
                        val expandedStates = remember {
                            mutableStateMapOf<Int, Boolean>().apply {
                                // 最初の日付（index 0）はデフォルトで展開
                                put(0, true)
                            }
                        }

                        historyData.forEachIndexed { index, dateGroup ->
                            DateGroupSection(
                                dateGroup = dateGroup,
                                index = index,
                                isExpanded = expandedStates.getOrDefault(index, false),
                                onExpandedChange = { expandedStates[index] = it },
                                scrollState = scrollState,
                                screenHeight = screenHeight,
                                navController = navController,
                                suggestionViewModel = suggestionViewModel,
                                coordinateDataMap = coordinateDataMap
                            )
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}