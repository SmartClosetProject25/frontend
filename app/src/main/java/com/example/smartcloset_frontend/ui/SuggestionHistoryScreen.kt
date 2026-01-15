package com.example.smartcloset_frontend.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ErrorOutline
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import android.util.Log
import androidx.navigation.NavHostController
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.ImageLoader
import coil.request.ImageRequest
import coil.imageLoader
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.data.CoordinateData
import com.example.smartcloset_frontend.data.CoordinateItem
import com.example.smartcloset_frontend.data.Item
import com.example.smartcloset_frontend.data.Items
import com.example.smartcloset_frontend.data.Proposal
import com.example.smartcloset_frontend.network.ServerUrlHolder
import com.example.smartcloset_frontend.viewmodel.SuggestionHistoryViewModel
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel
import java.text.SimpleDateFormat
import java.util.*

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
            val scrollState = rememberScrollState()
            val density = LocalDensity.current
            val screenHeight = with(density) { 
                androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp.toPx()
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
                        historyData.forEachIndexed { index, dateGroup ->
                            DateGroupSection(
                                dateGroup = dateGroup,
                                index = index,
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

@Composable
fun DateGroupSection(
    dateGroup: HistoryDateGroup,
    index: Int,
    scrollState: ScrollState,
    screenHeight: Float,
    navController: NavHostController,
    suggestionViewModel: SuggestionViewModel,
    coordinateDataMap: Map<Int, CoordinateData>
) {
    val context = LocalContext.current
    val imageLoader = context.imageLoader
    val density = LocalDensity.current
    
    // セクションの位置を追跡
    var sectionTopY by remember { mutableStateOf<Float?>(null) }
    var sectionHeight by remember { mutableStateOf<Float?>(null) }
    
    // 画像プリロード状態
    var imagesPreloaded by remember(dateGroup) { mutableStateOf(false) }
    var shouldPreload by remember { mutableStateOf(false) }
    
    // すべての画像URLを収集
    val imageUrls = remember(dateGroup) {
        val urls = mutableListOf<String>()
        dateGroup.suggestions.forEach { suggestion ->
            // アイテム画像
            listOfNotNull(
                suggestion.outer?.image_path,
                suggestion.top.image_path,
                suggestion.bottom.image_path
            ).forEach { imagePath ->
                buildImageUrl(imagePath)?.let { urls.add(it) }
            }
            // 生成画像
            suggestion.genimgPath?.let { genimgPath ->
                buildImageUrl(genimgPath)?.let { urls.add(it) }
            }
        }
        urls.distinct()
    }
    
    // スクロール位置を監視して可視領域を判定
    LaunchedEffect(scrollState.value, sectionTopY, sectionHeight) {
        val scrollY = with(density) { scrollState.value.toFloat() }
        val viewportBottom = scrollY + screenHeight
        
        // 最初の2グループは即座にプリロード
        if (index < 2) {
            shouldPreload = true
        } else if (sectionTopY != null && sectionHeight != null) {
            // セクションが表示領域に入ったか判定（少し前からプリロード開始）
            val preloadThreshold = screenHeight * 0.5f // 画面の50%手前からプリロード
            val sectionBottom = sectionTopY!! + sectionHeight!!
            shouldPreload = sectionTopY!! < viewportBottom + preloadThreshold
        }
    }
    
    // 画像をプリロードする
    LaunchedEffect(dateGroup, shouldPreload) {
        if (shouldPreload && imageUrls.isNotEmpty() && !imagesPreloaded) {
            try {
                // すべての画像を並列でプリロード
                val preloadJobs = imageUrls.map { imageUrl ->
                    async {
                        try {
                            val request = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .build()
                            imageLoader.execute(request)
                        } catch (e: Exception) {
                            Log.e("SuggestionHistoryScreen", "画像プリロードエラー ($imageUrl): ${e.message}", e)
                        }
                    }
                }
                // すべてのプリロードが完了するまで待機
                preloadJobs.awaitAll()
                imagesPreloaded = true
            } catch (e: Exception) {
                Log.e("SuggestionHistoryScreen", "画像プリロードエラー: ${e.message}", e)
                imagesPreloaded = true
            }
        }
    }

    Column(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            val position = coordinates.positionInRoot()
            sectionTopY = position.y
            sectionHeight = coordinates.size.height.toFloat()
        }
    ) {
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

        // 画像プリロード中はローディング表示
        if (!imagesPreloaded && imageUrls.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        } else {
            // 横スクロールカード
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(dateGroup.suggestions) { suggestion ->
                    HistoryCoordinateCard(
                        suggestion = suggestion,
                        onClick = {
                            navigateToGeneratedResult(
                                suggestion = suggestion,
                                navController = navController,
                                suggestionViewModel = suggestionViewModel,
                                coordinateDataMap = coordinateDataMap
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryCoordinateCard(
    suggestion: HistoryCoordinate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
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

@Composable
fun HistoryItemGridCell(
    item: CoordinateItem,
    label: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 画像
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f) // 縦長の比率
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE0E0E0))
                .border(1.dp, Color(0xFFC0C0C0), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            val imageUrl = buildImageUrl(item.image_path)
            if (imageUrl != null) {
                // リトライ用のキー
                var retryKey by remember { mutableStateOf(0) }
                val imageUrlWithRetry = remember(imageUrl, retryKey) {
                    if (retryKey > 0) {
                        val separator = if (imageUrl.contains("?")) "&" else "?"
                        "$imageUrl${separator}_retry=$retryKey"
                    } else {
                        imageUrl
                    }
                }
                
                SubcomposeAsyncImage(
                    model = imageUrlWithRetry,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = { state ->
                        val error = state.result.throwable
                        Log.e("SuggestionHistoryScreen", "画像読み込みエラー: ${error?.message}", error)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { retryKey++ },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "タップ",
                                    color = Color.Gray,
                                    fontSize = 7.sp
                                )
                            }
                        }
                    }
                )
            } else {
                Text(label, color = Color.Gray, fontSize = 9.sp)
            }
        }
        
        // ラベルとアイテム名
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun HistoryGeneratedImageCell(
    imagePath: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 画像
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f) // 縦長の比率
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE0E0E0))
                .border(1.dp, Color(0xFFC0C0C0), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            val imageUrl = buildImageUrl(imagePath)
            if (imageUrl != null) {
                // リトライ用のキー
                var retryKey by remember { mutableStateOf(0) }
                val imageUrlWithRetry = remember(imageUrl, retryKey) {
                    if (retryKey > 0) {
                        val separator = if (imageUrl.contains("?")) "&" else "?"
                        "$imageUrl${separator}_retry=$retryKey"
                    } else {
                        imageUrl
                    }
                }
                
                SubcomposeAsyncImage(
                    model = imageUrlWithRetry,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = { state ->
                        val error = state.result.throwable
                        Log.e("SuggestionHistoryScreen", "画像読み込みエラー: ${error?.message}", error)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { retryKey++ },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "タップ",
                                    color = Color.Gray,
                                    fontSize = 7.sp
                                )
                            }
                        }
                    }
                )
            } else {
                Text(label, color = Color.Gray, fontSize = 9.sp)
            }
        }
        
        // ラベル
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
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
    val genimgPath: String?,
    val tags: List<String>
)

// ヘルパー関数: 画像URLを構築
private fun buildImageUrl(imagePath: String): String? {
    if (imagePath.isBlank()) return null
    return if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
        imagePath
    } else {
        val baseUrl = (ServerUrlHolder.overrideBaseUrl ?: BuildConfig.SERVER_URL).trimEnd('/')
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

// ヘルパー関数: CoordinateItemからItemに変換
private fun coordinateItemToItem(coordinateItem: CoordinateItem): Item {
    return Item(
        id = coordinateItem.id,
        item_name = coordinateItem.name,
        image_path = coordinateItem.image_path,
        taste = emptyList() // CoordinateItemにはtasteがないため空リスト
    )
}

// ヘルパー関数: CoordinateDataからProposalを作成
private fun createProposalFromCoordinateData(coordinate: CoordinateData): Proposal {
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

// ナビゲーション関数: 生成結果画面に移動
private fun navigateToGeneratedResult(
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
        Log.e("SuggestionHistoryScreen", "Coordinate data not found for id: ${suggestion.id}")
    }
}
