package com.example.smartcloset_frontend.ui

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.smartcloset_frontend.data.RadarAxisDto
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcloset_frontend.data.repository.ProfileRepository
import com.example.smartcloset_frontend.network.ApiService
import com.example.smartcloset_frontend.network.RetrofitClient
import com.example.smartcloset_frontend.ui.networkErr.ProfileUiState
import com.example.smartcloset_frontend.viewmodel.ProfileViewModel
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModel

// ====================================================================
// 1. データ構造 (Data Classes)
// ====================================================================
//// 身体測定値の構造を定義
data class BodyMeasurements(
    val heightCm: String,
    val weightKg: String
)
fun axesToRadarValues(axes: List<RadarAxisDto>): Pair<List<Int>, List<String>> {
    val values = axes.map { (it.norm01 * 100.0).toInt().coerceIn(0, 100) }
    val labels = axes.map { it.label }
    return values to labels
}

// ====================================================================
// 2. UIコンポーネント (Composable Functions)
// ====================================================================

// プロフィール画面全体のUIを定義するメインのComposable関数
@Composable
fun ProfileScreen(
    navController: NavHostController,
    userSessionViewModel : UserSessionViewModel,
    api: ApiService = RetrofitClient.instance
) {
    val userId by userSessionViewModel.userId.collectAsState()
    val vm: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(
                    repository = ProfileRepository()
                ) as T
            }
        }
    )
    val state by vm.uiState.collectAsState()

    LaunchedEffect(userId) {
        vm.load(userId ?: 1)
    }

    // 画面全体を縦方向に配置し、スクロール可能にする
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()) // スクロール機能を有効化
            .padding(16.dp)
    ) {
        // 各UIセクションを呼び出す
        when (val s = state) {
            is ProfileUiState.Loading -> {
                Text("読み込み中…")
            }
            is ProfileUiState.Error -> {
                Text("取得失敗: ${s.message}", color = Color.Red)
                Log.e("ProfileScreen", "Error loading profile: ${s.message}")
            }
            is ProfileUiState.Success -> {
                val d = s.data

                // 既存UIに合わせて変換（null対策）
                val name = d.profile.userName.ifBlank { "ゲスト" }
//                val gender = d.profile.gender ?: "未設定"
                val height = d.profile.height?.toString() ?: ""
                val weight = d.profile.weight?.toString() ?: ""
                val genderText = when (d.profile.gender) {
                    0 -> "未設定"
                    1 -> "男性"
                    2 -> "女性"
                    else -> "その他"
                }

                HeaderSection(name, genderText, navController)
                Spacer(modifier = Modifier.height(24.dp))
                StatsSection(d.counts.itemCount, d.counts.coordinateCount, d.counts.favoriteCount)
                Spacer(modifier = Modifier.height(24.dp))

                // ★ここが変更点：6軸固定 → axes(8軸)へ
                RadarChartSection(d.axes)

                Spacer(modifier = Modifier.height(24.dp))
                DetailsSection(
                    body = BodyMeasurements(heightCm = height, weightKg = weight),
                    color = d.personalColor.colorName,
                    frame = "未設定" // frameTypeはバックに無いので今は固定でOK
                )
            }
            else -> {
                // 何も表示しない
            }
        }
    }
}

// ヘッダーセクション（アバター、名前、編集ボタン）
@Composable
fun HeaderSection(name: String, gender: String, navController: NavHostController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // 要素を左右に配置
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // アバター画像のプレースホルダー
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.LightGray, RoundedCornerShape(36.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = gender, color = Color.Gray)
            }
        }
        // 編集ボタン：クリックでプロフィール編集画面に遷移
        IconButton(onClick = { navController.navigate("profile_edit") }) {
            Icon(Icons.Default.Edit, contentDescription = "プロフィールを編集")
        }
    }
}

// 統計情報セクション（アイテム数、コーデ数、お気に入り数）
@Composable
fun StatsSection(items: Int, coords: Int, likes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround // 均等に配置
    ) {
        StatItem(value = items.toString(), label = "アイテム")
        StatItem(value = coords.toString(), label = "コーデ")
        StatItem(value = likes.toString(), label = "お気に入り")
    }
}

// 個別の統計情報を表示するコンポーネント
@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Light)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

// レーダーチャートセクション
@Composable
fun RadarChartSection(axes: List<RadarAxisDto>) {
    val (values, labels) = remember(axes) { axesToRadarValues(axes) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp), // ラベル表示のために高さを確保
        contentAlignment = Alignment.Center
    ) {
        RadarChart(values = values, labels = labels, maxValue = 100f)
    }
}

// レーダーチャートをカスタム描画するコンポーネント
@Composable
fun RadarChart(values: List<Int>, labels: List<String>, maxValue: Float = 100f) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.7f // ラベルのスペースを考慮して半径を調整
        val numPoints = values.size
        val angleStep = 360f / numPoints
        val webLevels = 4 // 網線の数

        // 1. 背景の網線を描画
        (1..webLevels).forEach { level ->
            val path = Path()
            val webRadius = radius * (level.toFloat() / webLevels)
            (0 until numPoints).forEach { i ->
                val angle = (i * angleStep - 90f) * (Math.PI / 180f).toFloat()
                val x = center.x + webRadius * cos(angle)
                val y = center.y + webRadius * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = Color.Gray, style = Stroke(width = 1f))
        }

        // 2. 中心から各頂点への軸線を描画
        repeat(numPoints) { i ->
            val angle = (i * angleStep - 90f) * (Math.PI / 180f).toFloat()
            val endPoint = Offset(x = center.x + radius * cos(angle), y = center.y + radius * sin(angle))
            drawLine(Color.Gray, center, endPoint, strokeWidth = 1f)
        }

        // 3. データに基づいた図形を描画
        val dataPath = Path()
        values.forEachIndexed { index, value ->
            val percentage = value / maxValue
            val currentRadius = radius * percentage
            val angle = (index * angleStep - 90f) * (Math.PI / 180f).toFloat()
            val x = center.x + currentRadius * cos(angle)
            val y = center.y + currentRadius * sin(angle)
            if (index == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()

        drawPath(dataPath, color = Color(0xFF81D4FA).copy(alpha = 0.7f)) // 塗りつぶし
        drawPath(dataPath, color = Color(0xFF03A9F4), style = Stroke(width = 4f)) // 枠線
        
        // 4. ラベルを描画
        val labelRadius = radius * 1.3f // ラベルの位置を調整
        labels.forEachIndexed { i, label ->
            val angle = (i * angleStep - 90f) * (Math.PI / 180f).toFloat()
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(label),
                style = TextStyle(color = Color.DarkGray, fontSize = 12.sp, textAlign = TextAlign.Center)
            )
            val textCenter = Offset(x = center.x + labelRadius * cos(angle), y = center.y + labelRadius * sin(angle))
            val textTopLeft = Offset(x = textCenter.x - textLayoutResult.size.width / 2, y = textCenter.y - textLayoutResult.size.height / 2)
            drawText(textLayoutResult, topLeft = textTopLeft)
        }
    }
}

// 詳細情報セクション（身体測定、診断結果）
@Composable
fun DetailsSection(body: BodyMeasurements, color: String, frame: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DataCard(title = "身体測定", modifier = Modifier.weight(1f)) {
            DataRow("身長", "${body.heightCm}cm")
            DataRow("体重", "${body.weightKg}kg")
        }
        DataCard(title = "診断", modifier = Modifier.weight(1f)) {
            DataRow("パーソナルカラー", color)
//            DataRow("骨格", frame)
        }
    }
}

// 汎用的な情報表示カード
@Composable
fun DataCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier, shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

// カード内の1行データ（ラベルと値を縦に表示）
@Composable
fun DataRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

// ====================================================================
// 3. プレビュー
// ====================================================================
//TODOこれなに？
//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreview() {
//    ProfileScreen(navController = rememberNavController(), userId = 1)
//}
