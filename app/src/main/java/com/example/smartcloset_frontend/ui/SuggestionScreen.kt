package com.example.smartcloset_frontend.ui

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartcloset_frontend.data.Proposal
import com.example.smartcloset_frontend.data.TodayPlanData
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ------------------- データ -------------------
data class CoordinateSuggestion(
    val id: String,
    val outerImageUrl: String?,
    val innerImageUrl: String?,
    val bottomImageUrl: String?,
    val tags: List<String>
)


// ------------------- メイン画面 -------------------
@Composable
fun SuggestionScreen(
    navController: NavHostController,
    suggestionViewModel: SuggestionViewModel = viewModel()
) {

    val todayPlan = remember { mutableStateOf("") }
    val context = LocalContext.current
    val isSending by suggestionViewModel.isSendingPlan.collectAsState()
    val isGeneratingImage by suggestionViewModel.isGeneratingImage.collectAsState()
    val proposals by suggestionViewModel.proposals.collectAsState()
    val toastMessage by suggestionViewModel.toastMessage.collectAsState()
    val navigateToGenerate by suggestionViewModel.navigateToGenerate.collectAsState()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            suggestionViewModel.onToastShown()
        }
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
                    Text("日本 - 愛知 - 名古屋", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌧️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("22℃", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column {
                            Text("降水確率", fontSize = 12.sp, color = Color.Gray)
                            Text("90%", fontSize = 14.sp)
                        }
                        Column {
                            Text("湿度", fontSize = 12.sp, color = Color.Gray)
                            Text("65%", fontSize = 14.sp)
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

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (todayPlan.value.isNotBlank()) {
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val todayPlanData = TodayPlanData(
                        plan = todayPlan.value,
                        date = currentDate,
                        location = "日本 - 愛知 - 名古屋",
                        weather = "22℃",
                        precipitation = "90%",
                        humidity = "65%"
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
                    modifier = Modifier.width(cardWidth)
                )
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
fun ItemDisplay(imageUrl: String?, label: String, tags: List<String>) {
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
            if (imageUrl != null) {
                AsyncImage(model = imageUrl, contentDescription = label)
            } else {
                Text(label, color = Color.Gray, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ----- テキスト -----
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {

                tags.forEach { tag ->
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
    modifier: Modifier = Modifier
) {

    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ItemDisplay(null, "アウター", listOf(proposal.items.outer ?: ""))
                ItemDisplay(null, "インナー", listOf(proposal.items.tops ?: ""))
                ItemDisplay(null, "ボトムス", listOf(proposal.items.bottoms ?: ""))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(proposal.item_ids.joinToString(), style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = Color(0xFFE0E0E0))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 👍
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        if (isLiked) isDisliked = false
                    }
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = if (isLiked) Color(0xFF2196F3) else Color.Gray
                    )
                }

                // 👎
                IconButton(
                    onClick = {
                        isDisliked = !isDisliked
                        if (isDisliked) isLiked = false
                    }
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier.rotate(180f),
                        tint = if (isDisliked) Color(0xFFE53935) else Color.Gray
                    )
                }

                // ✨生成ボタン → generate へ遷移
                IconButton(
                    onClick = { suggestionViewModel.generateImage(proposal.item_ids) },
                    enabled = !isGeneratingImage
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF00C853), RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGeneratingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
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
                        }
                    }
                }
            }
        }
    }
}
