package com.example.smartcloset_frontend.ui.suggestion

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartcloset_frontend.data.LocationData
import com.example.smartcloset_frontend.data.Proposal
import com.example.smartcloset_frontend.data.TodayPlanData
import com.example.smartcloset_frontend.ui.common.ImagePreloader
import com.example.smartcloset_frontend.ui.common.ImageUrlHelper
import com.example.smartcloset_frontend.ui.suggestion.components.CoordinateCard
import com.example.smartcloset_frontend.ui.suggestion.components.TodayPlanSection
import com.example.smartcloset_frontend.ui.suggestion.components.WeatherCard
import com.example.smartcloset_frontend.utils.GetLocation
import com.example.smartcloset_frontend.utils.WeatherLocationLoader
import com.example.smartcloset_frontend.viewmodel.GetWeatherViewModel
import com.example.smartcloset_frontend.viewmodel.ItemViewModel
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 提案画面（位置情報以外）
 */
@Composable
fun SuggestionScreen(
    navController: NavHostController,
    suggestionViewModel: SuggestionViewModel = viewModel(),
    getWeatherViewModel: GetWeatherViewModel = viewModel(),
    userSessionViewModel: UserSessionViewModel,
) {
    val userId by userSessionViewModel.userId.collectAsState()
    val context = LocalContext.current
    val weatherData by getWeatherViewModel.weatherData.collectAsState()
    val todayPlan = remember { mutableStateOf("") }
    val gender = remember { mutableStateOf<String?>(null) }
    val isSending by suggestionViewModel.isSendingPlan.collectAsState()
    val isGeneratingImage by suggestionViewModel.isGeneratingImage.collectAsState()
    val proposals by suggestionViewModel.proposals.collectAsState()

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

    val lazyListState = rememberLazyListState()
    val coroutine = rememberCoroutineScope()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = 320.dp
    val padding = (screenWidth - cardWidth) / 2

    val cardPx = with(LocalDensity.current) { cardWidth.toPx() }

    // 画像プリロード状態
    var isPreloadingImages by remember { mutableStateOf(false) }
    var imagesPreloaded by remember { mutableStateOf(false) }

    // すべての画像URLを収集
    val imageUrls = remember(proposals) {
        proposals.flatMap { proposal ->
            listOfNotNull(
                proposal.items.outer?.image_path,
                proposal.items.tops?.image_path,
                proposal.items.bottoms?.image_path
            )
        }
    }.let { paths ->
        ImageUrlHelper.buildImageUrls(paths)
    }

    // 画像をプリロードする
    LaunchedEffect(proposals) {
        if (proposals.isNotEmpty() && !imagesPreloaded && imageUrls.isNotEmpty()) {
            isPreloadingImages = true
            ImagePreloader.preloadImages(context, imageUrls)
            imagesPreloaded = true
            isPreloadingImages = false
        }
    }

    // スナップ処理
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val first = lazyListState.firstVisibleItemIndex
            val offset = lazyListState.firstVisibleItemScrollOffset

            val target = if (offset > cardPx / 2) first + 1 else first

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

        // 天気カード
        WeatherCard(weatherData = weatherData)

        Spacer(modifier = Modifier.height(24.dp))

        // 今日の予定セクション
        TodayPlanSection(
            todayPlan = todayPlan.value,
            onPlanChange = { todayPlan.value = it },
            isSending = isSending,
            gender = gender.value,
            onGenderChange = { gender.value = it },
            onSendClick = {
                if (todayPlan.value.isNotBlank()) {
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val sendLocation = weatherData?.location ?: "不明"
                    val sendWeather = weatherData?.tempC?.let { "${it}℃" } ?: "--℃"
                    val sendPrecip = weatherData?.precipitationPercent?.let { "${it}%" } ?: "--%"
                    val sendHumidity = weatherData?.humidityPercent?.let { "${it}%" } ?: "--%"

                    val todayPlanData = TodayPlanData(
                        id = userId,
                        plan = todayPlan.value,
                        date = currentDate,
                        location = sendLocation,
                        weather = sendWeather,
                        precipitation = sendPrecip,
                        humidity = sendHumidity,
                        gender = gender.value
                    )
                    suggestionViewModel.sendTodayPlan(todayPlanData)
                } else {
                    Toast.makeText(context, "予定を入力してください", Toast.LENGTH_SHORT).show()
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // おすすめ一覧
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