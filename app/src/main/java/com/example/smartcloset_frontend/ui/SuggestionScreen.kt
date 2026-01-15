package com.example.smartcloset_frontend.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.ImageLoader
import coil.request.ImageRequest
import coil.imageLoader
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.data.Item
import com.example.smartcloset_frontend.data.JudgeRequestData
import com.example.smartcloset_frontend.data.LocationData
import com.example.smartcloset_frontend.data.Proposal
import com.example.smartcloset_frontend.data.TodayPlanData
import com.example.smartcloset_frontend.network.ServerUrlHolder
import com.example.smartcloset_frontend.utils.GetLocation
import com.example.smartcloset_frontend.utils.ImageUtils
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel
import com.example.smartcloset_frontend.viewmodel.GetWeatherViewModel
import com.example.smartcloset_frontend.utils.WeatherLocationLoader
import com.example.smartcloset_frontend.viewmodel.ItemViewModel
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ------------------- メイン画面 -------------------
@Composable
fun SuggestionScreen(
    navController: NavHostController,
    suggestionViewModel: SuggestionViewModel = viewModel(),
    getWeatherViewModel: GetWeatherViewModel = viewModel(),
    userSessionViewModel : UserSessionViewModel,
) {
    val userId by userSessionViewModel.userId.collectAsState()
    val context = LocalContext.current
    val weatherData by getWeatherViewModel.weatherData.collectAsState()
    val weatherError by getWeatherViewModel.error.collectAsState()
    val todayPlan = remember { mutableStateOf("") }
    val isSending by suggestionViewModel.isSendingPlan.collectAsState()
    val isGeneratingImage by suggestionViewModel.isGeneratingImage.collectAsState()
    val proposals by suggestionViewModel.proposals.collectAsState()
    val toastMessage by suggestionViewModel.toastMessage.collectAsState()
    val navigateToGenerate by suggestionViewModel.navigateToGenerate.collectAsState()

    LaunchedEffect(toastMessage ) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            suggestionViewModel.onToastShown()
        }
    }
    LaunchedEffect(Unit) {
        try {
            val loc = GetLocation.getLastLocationSuspend(context)
            getWeatherViewModel.fetchWeather(
                LocationData(lat = loc.latitude, lon = loc.longitude)
            )
        } catch (e: Exception) {
            Log.e("Weather", "Location error: ${e.message}", e)
        }
    }
    // 位置情報パーミッションの許可
    WeatherLocationLoader(getWeatherViewModel)

    val locationText = weatherData?.location ?: "取得中..."
    val tempText = weatherData?.tempC?.let { "${it}℃" } ?: "--℃"
    val popText = weatherData?.precipitationPercent?.let { "${it}%" } ?: "--%"
    val humText = weatherData?.humidityPercent?.let { "${it}%" } ?: "--%"
    val emoji = when (weatherData?.today3h?.firstOrNull()?.weatherType) {
        "clear" -> "☀️"
        "rain" -> "🌧️"
        "snow" -> "❄️"
        "cloud" -> "☁️"
        else -> "☁️"
    }

    LaunchedEffect(navigateToGenerate) {
        if (navigateToGenerate) {
            navController.navigate("generate")
            suggestionViewModel.onGenerateScreenNavigated()
        }
    }

    val lazyListState = rememberLazyListState()
    val coroutine = rememberCoroutineScope()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = 320.dp
    val padding = (screenWidth - cardWidth) / 2

    val cardPx = with(LocalDensity.current) { cardWidth.toPx() }

    // 画像プリロード状態
    var isPreloadingImages by remember { mutableStateOf(false) }
    var imagesPreloaded by remember { mutableStateOf(false) }
    val imageLoader = context.imageLoader

    // すべての画像URLを収集する関数
    fun collectImageUrls(proposals: List<Proposal>): List<String> {
        val imageUrls = mutableListOf<String>()
        val baseUrl = (ServerUrlHolder.overrideBaseUrl ?: BuildConfig.SERVER_URL).trimEnd('/')
        
        proposals.forEach { proposal ->
            listOfNotNull(
                proposal.items.outer?.image_path,
                proposal.items.tops?.image_path,
                proposal.items.bottoms?.image_path
            ).forEach { imagePath ->
                val imageUrl = if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                    imagePath
                } else {
                    val path = if (imagePath.startsWith("/")) imagePath else "/$imagePath"
                    "$baseUrl$path"
                }
                if (imageUrl.isNotBlank()) {
                    imageUrls.add(imageUrl)
                }
            }
        }
        return imageUrls.distinct()
    }

    // 画像をプリロードする
    LaunchedEffect(proposals) {
        if (proposals.isNotEmpty() && !imagesPreloaded) {
            isPreloadingImages = true
            val imageUrls = collectImageUrls(proposals)
            
            if (imageUrls.isNotEmpty()) {
                try {
                    // すべての画像を並列でプリロード（実際に読み込む）
                    val preloadJobs = imageUrls.map { imageUrl ->
                        async {
                            try {
                                val request = ImageRequest.Builder(context)
                                    .data(imageUrl)
                                    .build()
                                imageLoader.execute(request)
                            } catch (e: Exception) {
                                Log.e("SuggestionScreen", "画像プリロードエラー ($imageUrl): ${e.message}", e)
                                // 個別のエラーは無視して続行
                            }
                        }
                    }
                    // すべてのプリロードが完了するまで待機
                    preloadJobs.awaitAll()
                    
                    imagesPreloaded = true
                } catch (e: Exception) {
                    Log.e("SuggestionScreen", "画像プリロードエラー: ${e.message}", e)
                    // エラーが発生しても表示は続行
                    imagesPreloaded = true
                }
            } else {
                imagesPreloaded = true
            }
            isPreloadingImages = false
        }
    }

    // -------- スナップ処理 --------
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val first = lazyListState.firstVisibleItemIndex
            val offset = lazyListState.firstVisibleItemScrollOffset

            val target =
                if (offset > cardPx / 2) first + 1 else first

            coroutine.launch {
                lazyListState.animateScrollToItem(target)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // ------------------- 天気カード -------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(locationText, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tempText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column {
                            Text("降水確率", fontSize = 12.sp, color = Color.Gray)
                            Text(popText, fontSize = 14.sp)
                        }
                        Column {
                            Text("湿度", fontSize = 12.sp, color = Color.Gray)
                            Text(humText, fontSize = 14.sp)
                        }
                    }
                }
                // 3時間ごとの天気予報
                val list = weatherData?.today3h.orEmpty()
                if (list.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(list) { h ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(h.timeLabel, fontSize = 12.sp, color = Color.Gray)
                                Text("${h.tempC}℃", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${h.precipitationPercent}%", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ------------------- 今日の予定 -------------------
        Text(
            "今日の予定",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = todayPlan.value,
            onValueChange = { todayPlan.value = it },
            placeholder = { Text("ランチ", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true,
            enabled = !isSending
        )

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
                    onClick = {
                        todayPlan.value = suggestion
                    },
                    enabled = !isSending,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
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
            onClick = {
                if (todayPlan.value.isNotBlank()) {
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val sendLocation = weatherData?.location ?: "不明"
                    val sendWeather = weatherData?.tempC?.let { "${it}℃" } ?: "--℃"
                    val sendPrecip = weatherData?.precipitationPercent?.let { "${it}%" } ?: "--%"
                    val sendHumidity = weatherData?.humidityPercent?.let { "${it}%" } ?: "--%"
                    val userId = userId

                    val todayPlanData = TodayPlanData(
                        id = userId,
                        plan = todayPlan.value,
                        date = currentDate,
                        location = sendLocation,
                        weather = sendWeather,
                        precipitation = sendPrecip,
                        humidity = sendHumidity
                    )
                    suggestionViewModel.sendTodayPlan(todayPlanData)
                } else {
                    Toast.makeText(context, "予定を入力してください", Toast.LENGTH_SHORT).show()
                }
            },
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

        Spacer(modifier = Modifier.height(32.dp))

        // ------------------- おすすめ一覧 -------------------
        Text(
            "おすすめのコーディネート",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        // 画像プリロード中または未完了の場合はローディング表示
        if (isPreloadingImages || (proposals.isNotEmpty() && !imagesPreloaded)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "画像を読み込み中...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else if (proposals.isNotEmpty()) {
            // プリロード完了後にLazyRowを表示
            LazyRow(
                state = lazyListState,
                contentPadding = PaddingValues(horizontal = padding),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(proposals) { proposal ->
                    CoordinateCard(
                        proposal = proposal,
                        suggestionViewModel = suggestionViewModel,
                        isGeneratingImage = isGeneratingImage,
                        modifier = Modifier.width(cardWidth),
                        userSessionViewModel = userSessionViewModel,
                        itemViewModel = viewModel()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = { navController.navigate("suggestion_history") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("履歴を見る", color = Color.White)
        }

        Spacer(modifier = Modifier.height(26.dp))
    }
}


// ------------------- アイテム表示 -------------------
@Composable
fun ItemDisplay(item: Item?, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ----- 画像 -----
        Box(
            modifier = Modifier
                .size(width = 85.dp, height = 110.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFFC0C0C0), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (item != null && item.image_path.isNotBlank()) {
                val imageUrl = if (item.image_path.startsWith("http://") || item.image_path.startsWith("https://")) {
                    item.image_path
                } else {
                    val baseUrl = (ServerUrlHolder.overrideBaseUrl ?: BuildConfig.SERVER_URL).trimEnd('/')
                    val imagePath = if (item.image_path.startsWith("/")) item.image_path else "/${item.image_path}"
                    "$baseUrl$imagePath"
                }
                
                // リトライ用のキー
                var retryKey by remember { mutableStateOf(0) }
                val imageUrlWithRetry = remember(imageUrl, retryKey) {
                    imageUrl?.let { url ->
                        if (retryKey > 0) {
                            val separator = if (url.contains("?")) "&" else "?"
                            "$url${separator}_retry=$retryKey"
                        } else {
                            url
                        }
                    }
                }
                
                SubcomposeAsyncImage(
                    model = imageUrlWithRetry,
                    contentDescription = item.item_name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = { state ->
                        val error = state.result.throwable
                        Log.e("SuggestionScreen", "画像読み込みエラー: ${error?.message}", error)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { retryKey++ },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "タップして再読み込み",
                                    color = Color.Gray,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                )
            } else {
                Text(label, color = Color.Gray, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ----- テキスト -----
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item?.item_name ?: label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item?.taste?.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE0F7FA), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
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


// ------------------- コーディネートカード -------------------
@Composable
fun CoordinateCard(
    proposal: Proposal,
    suggestionViewModel: SuggestionViewModel,
    isGeneratingImage: Boolean,
    modifier: Modifier = Modifier,
    userSessionViewModel : UserSessionViewModel,
    itemViewModel: ItemViewModel
) {
    val userId by userSessionViewModel.userId.collectAsState()
    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }
    var isReasonExpanded by remember { mutableStateOf(false) }
    var showModelSelectionDialog by remember { mutableStateOf(false) }

    // カメラ撮影用のLauncher
    val cameraLauncherBitmap = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            showModelSelectionDialog = false
            // カメラで撮影した画像で生成を開始
            startImageGeneration(
                proposal = proposal,
                modelBitmap = it,
                modelUri = null,
                modelTemplate = null,
                context = context,
                suggestionViewModel = suggestionViewModel
            )
        }
    }

    // カメラ権限リクエスト用
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncherBitmap.launch(null)
        } else {
            Toast.makeText(context, "カメラの権限が必要です", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ItemDisplay(proposal.items.outer, "アウター")
                ItemDisplay(proposal.items.tops, "インナー")
                ItemDisplay(proposal.items.bottoms, "ボトムス")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 理由をアコーディオン形式で表示
            if (proposal.reason.isNotBlank()) {
                // 展開ボタン
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isReasonExpanded = !isReasonExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "詳細を見る",
                        fontSize = 12.sp,
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (isReasonExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isReasonExpanded) "折りたたむ" else "展開する",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 理由のテキスト（アニメーション付き）
                AnimatedVisibility(
                    visible = isReasonExpanded,
                    enter = expandVertically(
                        animationSpec = tween(300),
                        expandFrom = Alignment.Top
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(300),
                        shrinkTowards = Alignment.Top
                    )
                ) {
                    Text(
                        text = proposal.reason,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            Divider(color = Color(0xFFE0E0E0))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左側：✨生成ボタン（大きめ）
                Button(
                    onClick = {
                        showModelSelectionDialog = true
                    },
                    enabled = !isGeneratingImage,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (isGeneratingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "生成",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "生成",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 右側：評価ボタン（👍と👎）
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 👍評価ボタン
                    IconButton(
                        onClick = {
                            isLiked = !isLiked
                            if (isLiked) isDisliked = false
                            val data = JudgeRequestData(
                                userId = userId,
                                planItemId = 2,
                                vote = "good"
                            )
                            itemViewModel.sendJudge(data)
                        }
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = if (isLiked) Color(0xFF2196F3) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 👎評価ボタン
                    IconButton(
                        onClick = {
                            isDisliked = !isDisliked
                            if (isDisliked) isLiked = false
                            val data = JudgeRequestData(
                                userId = userId,
                                planItemId = 2,
                                vote = "bad"
                            )
                            itemViewModel.sendJudge(data)
                        }
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier
                                .rotate(180f)
                                .size(24.dp),
                            tint = if (isDisliked) Color(0xFFE53935) else Color.Gray
                        )
                    }
                }
            }
        }
    }

    // モデル選択ダイアログ
    if (showModelSelectionDialog) {
        ModelSelectionDialog(
            onDismiss = {
                showModelSelectionDialog = false
            },
            onCameraClick = {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (granted) {
                    cameraLauncherBitmap.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onMannequinClick = {
                showModelSelectionDialog = false
                startImageGeneration(
                    proposal = proposal,
                    modelBitmap = null,
                    modelUri = null,
                    modelTemplate = "mannequin",
                    context = context,
                    suggestionViewModel = suggestionViewModel
                )
            },
            onProfileClick = {
                showModelSelectionDialog = false
                startImageGeneration(
                    proposal = proposal,
                    modelBitmap = null,
                    modelUri = null,
                    modelTemplate = "profile",
                    context = context,
                    suggestionViewModel = suggestionViewModel
                )
            }
        )
    }
}

// 画像生成を開始する関数
fun startImageGeneration(
    proposal: Proposal,
    modelBitmap: Bitmap?,
    modelUri: Uri?,
    modelTemplate: String?,
    context: android.content.Context,
    suggestionViewModel: SuggestionViewModel
) {
    // proposal.itemsから各アイテムのimage_pathを取得
    val imagePaths = mutableListOf<String>()
    
    // 服の画像パスを追加
    imagePaths.addAll(
        listOfNotNull(
            proposal.items.outer?.image_path,
            proposal.items.tops?.image_path,
            proposal.items.bottoms?.image_path
        )
    )
    
    // モデル画像をbase64エンコード（カメラ撮影時のみ）
    val modelImageBase64: String? = when {
        modelBitmap != null -> ImageUtils.bitmapToBase64(modelBitmap)
        modelUri != null -> ImageUtils.uriToBase64(context, modelUri)
        else -> null
    }
    
    suggestionViewModel.generateImage(imagePaths, modelImageBase64, modelTemplate, proposal, proposal.coordinate_id)
}

// モデル選択ダイアログ
@Composable
fun ModelSelectionDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onMannequinClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "モデルを選択",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // カメラで撮影ボタン
                Button(
                    onClick = onCameraClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "カメラ",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "カメラで撮影",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // マネキンを使用ボタン
                Button(
                    onClick = onMannequinClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "マネキン",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "マネキンを使用",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // プロフィール画像を使用ボタン
                Button(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "プロフィール画像",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "プロフィール画像を使用",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = Color.Gray)
            }
        }
    )
}
