package com.example.smartcloset_frontend.ui

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
import kotlin.math.cos
import kotlin.math.sin

// ====================================================================
// 1. データ構造 (Data Classes)
// ====================================================================

// スタイルデータの構造を定義
data class StyleData(
    val casual: Int,
    val elegant: Int,
    val mode: Int,
    val mannishMasculine: Int,
    val street: Int,
    val natural: Int
)

// 身体測定値の構造を定義（簡略版）
data class BodyMeasurements(
    val heightCm: String,
    val weightKg: String
)

// ユーザープロフィールの全情報を持つ構造を定義（簡略版）
data class UserProfile(
    val name: String,
    val gender: String,
    val itemCount: Int,
    val coordCount: Int,
    val likeCount: Int,
    val styleData: StyleData,
    val bodyMeasurements: BodyMeasurements,
    val personalColor: String,
    val frameType: String
)

// ====================================================================
// 2. UIコンポーネント (Composable Functions)
// ====================================================================

// プロフィール画面全体のUIを定義するメインのComposable関数
@Composable
fun ProfileScreen(navController: NavHostController) {
    // 表示するための仮のユーザープロフィールデータを作成
    val profile = UserProfile(
        name = "はるたろう",
        gender = "Male",
        itemCount = 126,
        coordCount = 21,
        likeCount = 5,
        styleData = StyleData(casual = 90, elegant = 40, mode = 50, mannishMasculine = 80, street = 40, natural = 70),
        bodyMeasurements = BodyMeasurements(heightCm = "165", weightKg = "52"),
        personalColor = "イエベ",
        frameType = "ストレート"
    )

    // 画面全体を縦方向に配置し、スクロール可能にする
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()) // スクロール機能を有効化
            .padding(16.dp)
    ) {
        // 各UIセクションを呼び出す
        HeaderSection(profile.name, profile.gender, navController)
        Spacer(modifier = Modifier.height(24.dp))
        StatsSection(profile.itemCount, profile.coordCount, profile.likeCount)
        Spacer(modifier = Modifier.height(24.dp))
        RadarChartSection(profile.styleData)
        Spacer(modifier = Modifier.height(24.dp))
        DetailsSection(profile.bodyMeasurements, profile.personalColor, profile.frameType)
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
fun RadarChartSection(data: StyleData) {
    val values = listOf(data.casual, data.elegant, data.mode, data.mannishMasculine, data.street, data.natural)
    val labels = listOf("カジュアル", "エレガント", "モード", "マニッシュ/\nマスキュリン", "ストリート", "ナチュラル")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp), // ラベル表示のために高さを確保
        contentAlignment = Alignment.Center
    ) {
        RadarChart(values = values, labels = labels)
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
            DataRow("骨格", frame)
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

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}
