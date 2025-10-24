// パッケージ宣言：このファイルがプロジェクトのどのディレクトリ階層に属するかを示します。
package com.example.smartcloset_frontend.ui

// ====================================================================
// インポートセクション：UI描画に必要なライブラリや関数を読み込みます。
// ====================================================================

import androidx.compose.foundation.Canvas // カスタム描画（レーダーチャート）用
import androidx.compose.foundation.background // 背景色設定用
import androidx.compose.foundation.layout.* // レイアウト（Row, Column, Boxなど）全般
import androidx.compose.foundation.shape.RoundedCornerShape // 角丸設定用
import androidx.compose.material3.Card // カードUIコンポーネント
import androidx.compose.material3.Text // テキスト表示コンポーネント
import androidx.compose.runtime.Composable // Compose UI関数であることを示す
import androidx.compose.ui.Alignment // 配置設定（中央寄せなど）
import androidx.compose.ui.Modifier // UI要素の修飾子（サイズ、パディングなど）
import androidx.compose.ui.geometry.Offset // 座標指定
import androidx.compose.ui.graphics.Color // 色指定
import androidx.compose.ui.graphics.Path // 複数点を結ぶ図形描画用
import androidx.compose.ui.text.font.FontWeight // フォントの太さ
import androidx.compose.ui.tooling.preview.Preview // プレビュー表示用
import androidx.compose.ui.unit.dp // サイズ・余白の単位（dp）
import androidx.compose.ui.unit.sp // テキストサイズの単位（sp）
import kotlin.math.cos // 数学関数：コサイン
import kotlin.math.sin // 数学関数：サイン

// ====================================================================
// 1. データ構造 (Data Classes)
// データを保持するための不変な構造を定義します。
// ====================================================================

// スタイルレーダーチャートのデータを保持するデータクラス
data class StyleData(
    val casual: Int,
    val elegant: Int,
    val mode: Int,
    val mannishMasculine: Int, // マニッシュ・マスキュリン
    val street: Int,
    val natural: Int
)

// 身体測定値を保持するデータクラス
data class BodyMeasurements(
    val heightCm: String,       // 身長
    val weightKg: String,       // 体重
    val inseamCm: String,       // 股下
    val footSizeCm: String,     // 足
    val shoulderWidthCm: String // 肩幅
)

// スリーサイズを保持するデータクラス
data class SizeDetails(
    val bustCm: String,  // バスト
    val waistCm: String, // ウエスト
    val hipCm: String    // ヒップ
)

// ユーザーの全情報を保持するデータクラス
data class UserProfile(
    val name: String,
    val gender: String,
    val itemCount: Int,      // アイテム数
    val coordCount: Int,     // コーデ数
    val likeCount: Int,      // お気に入り数
    val styleData: StyleData, // スタイルレーダーチャートのデータ
    val bodyMeasurements: BodyMeasurements, // 身体測定値
    val sizeDetails: SizeDetails,           // スリーサイズ
    val personalColor: String, // パーソナルカラー
    val frameType: String      // 骨格タイプ
)

// ====================================================================
// 2. UIコンポーネント (Composable Functions)
// データを基に画面要素を構築する関数を定義します。
// ====================================================================

// 画面全体のUIを定義するメインのComposable関数
@Composable
fun ProfileScreen() {
    // 画面内で使用する仮のユーザープロフィールデータをインスタンス化
    val profile = UserProfile(
        name = "はるたろう",
        gender = "Male",
        itemCount = 126,
        coordCount = 21,
        likeCount = 5,
        // スタイルデータ（レーダーチャートの値）
        styleData = StyleData(casual = 90, elegant = 40, mode = 50, mannishMasculine = 80, street = 40, natural = 70),
        // 身体測定値
        bodyMeasurements = BodyMeasurements(heightCm = "165", weightKg = "52", inseamCm = "75", footSizeCm = "24", shoulderWidthCm = "38"),
        // スリーサイズ
        sizeDetails = SizeDetails(bustCm = "85", waistCm = "63", hipCm = "88"),
        personalColor = "イエベ",
        frameType = "ストレート"
    )

    // 画面全体を縦方向に配置するコンテナ
    Column(
        modifier = Modifier
            .fillMaxSize() // 画面全体を使う
            .background(Color.White) // 背景色を白に設定
            .padding(16.dp) // 外側にパディングを設定
    ) {
        // 1. ヘッダーセクション（名前、性別、アバター）の呼び出し
        HeaderSection(profile.name, profile.gender)
        Spacer(modifier = Modifier.height(24.dp)) // 縦の余白

        // 2. 統計情報セクション（アイテム数など）の呼び出し
        StatsSection(profile.itemCount, profile.coordCount, profile.likeCount)
        Spacer(modifier = Modifier.height(24.dp)) // 縦の余白

        // 3. スタイルレーダーチャートセクションの呼び出し
        RadarChartSection(profile.styleData)
        Spacer(modifier = Modifier.height(24.dp)) // 縦の余白

        // 4. 下部詳細情報セクション（測定値、カラー、骨格）の呼び出し
        DetailsSection(
            profile.bodyMeasurements,
            profile.sizeDetails,
            profile.personalColor,
            profile.frameType
        )
    }
}

// ユーザー名と性別のヘッダーを横並びに表示
@Composable
fun HeaderSection(name: String, gender: String) {
    Row(
        modifier = Modifier.fillMaxWidth(), // 親要素の幅いっぱい
        verticalAlignment = Alignment.CenterVertically // 垂直方向中央寄せ
    ) {
        // アバター画像のプレースホルダー（灰色の円）
        Box(
            modifier = Modifier
                .size(72.dp) // サイズを72dpに
                .background(Color.LightGray, RoundedCornerShape(36.dp)) // 灰色の円形
        )
        Spacer(modifier = Modifier.width(16.dp)) // 横の余白
        Column {
            Text(text = name, fontSize = 24.sp, fontWeight = FontWeight.Bold) // ユーザー名
            Text(text = gender, color = Color.Gray) // 性別
        }
    }
}

// アイテム数、コーデ数、お気に入り数を横並びに表示
@Composable
fun StatsSection(items: Int, coords: Int, likes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround // 子要素を均等に配置
    ) {
        StatItem(value = items.toString(), label = "アイテム")
        StatItem(value = coords.toString(), label = "コーデ")
        StatItem(value = likes.toString(), label = "お気に入り")
    }
}

// 個別の統計値とラベルを縦に表示
@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { // 水平方向中央寄せ
        Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Light) // 数値（大きく細い）
        Text(text = label, fontSize = 12.sp, color = Color.Gray) // ラベル（小さく灰色）
    }
}

// レーダーチャートのコンテナと描画ロジックを呼び出す
@Composable
fun RadarChartSection(data: StyleData) {
    val values = listOf(data.casual, data.elegant, data.mode, data.mannishMasculine, data.street, data.natural) // 描画する値のリスト

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp) // チャートの高さを固定
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        RadarChart(values = values) // 実際のチャート描画関数を呼び出し
    }
}

// シンプルなレーダーチャートをカスタム描画する（Canvas使用）
@Composable
fun RadarChart(values: List<Int>, maxValue: Float = 100f) {
    Canvas(modifier = Modifier.fillMaxSize()) { // 描画領域を確保
        val center = Offset(size.width / 2, size.height / 2) // 中心座標
        val radius = size.minDimension / 2 // 最大半径
        val numPoints = values.size // 軸の数
        val angleStep = 360f / numPoints // 軸間の角度

        // 枠線と軸の描画処理
        repeat(numPoints) { i ->
            val angle = (i * angleStep - 90f) * (Math.PI / 180f).toFloat() // 角度を計算
            val endPoint = Offset( // 軸の終点の座標を計算
                x = center.x + radius * cos(angle),
                y = center.y + radius * sin(angle)
            )
            drawLine(Color.LightGray, center, endPoint, strokeWidth = 1f) // 軸（中心から外側へ）を描画
        }

        // データエリアの描画処理
        val path = Path() // 図形の形状を定義するPathを初期化
        values.forEachIndexed { index, value ->
            val percentage = value / maxValue // 値の最大値に対する割合
            val currentRadius = radius * percentage // 軸上の描画点の半径
            val angle = (index * angleStep - 90f) * (Math.PI / 180f).toFloat()
            val x = center.x + currentRadius * cos(angle) // X座標
            val y = center.y + currentRadius * sin(angle) // Y座標

            if (index == 0) {
                path.moveTo(x, y) // 最初の点は開始点に設定
            } else {
                path.lineTo(x, y) // 2点目以降は線で結ぶ
            }
        }
        path.close() // 最後の点と最初の点を結んで図形を閉じる
        drawPath(path, color = Color(0xFF64B5F6).copy(alpha = 0.6f)) // 図形を青の半透明で塗りつぶす
        drawPath(path, color = Color(0xFF1976D2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)) // 図形の輪郭を濃い青の線で描画
    }
}

// 下部の3つの情報カード（身体測定、スリーサイズ、パーソナルカラー/骨格）を横並びに表示
@Composable
fun DetailsSection(
    body: BodyMeasurements,
    size: SizeDetails,
    color: String,
    frame: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween // 子要素間にスペースを設ける
    ) {
        // 身体測定カード
        DataCard(title = "身体測定", modifier = Modifier.weight(1f)) { // weight(1f)で均等幅に
            DataRow("身長", "${body.heightCm}cm")
            DataRow("体重", "${body.weightKg}kg")
            DataRow("股下", "${body.inseamCm}cm")
            DataRow("足", "${body.footSizeCm}cm")
            DataRow("肩幅", "${body.shoulderWidthCm}cm")
        }
        Spacer(modifier = Modifier.width(8.dp)) // カード間の横スペース

        // スリーサイズカード
        DataCard(title = "スリーサイズ", modifier = Modifier.weight(1f)) {
            DataRow("バスト", "${size.bustCm}cm")
            DataRow("ウエスト", "${size.waistCm}cm")
            DataRow("ヒップ", "${size.hipCm}cm")
        }
        Spacer(modifier = Modifier.width(8.dp)) // カード間の横スペース

        // パーソナルカラーと骨格タイプカード
        DataCard(title = "パーソナルカラー", modifier = Modifier.weight(1f)) {
            Text(text = color, fontSize = 16.sp, fontWeight = FontWeight.Bold) // パーソナルカラーの値
            Spacer(modifier = Modifier.height(16.dp)) // 縦の余白
            Text(text = "骨格", fontSize = 12.sp, color = Color.Gray) // 骨格のラベル
            Text(text = frame, fontSize = 16.sp, fontWeight = FontWeight.Bold) // 骨格タイプ
        }
    }
}

// 汎用的な情報カードコンポーネント
@Composable
fun DataCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.height(200.dp), // 高さを固定
        shape = RoundedCornerShape(8.dp), // 角丸を設定
    ) {
        Column(modifier = Modifier.padding(8.dp)) { // カード内部のコンテンツを縦に配置
            Text( // カードタイトル
                text = title,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            content() // 呼び出し元から渡されたコンテンツ（DataRowなど）を描画
        }
    }
}

// カード内部のラベルと値の1行を横並びに表示
@Composable
fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // 左右に要素を配置
        verticalAlignment = Alignment.Bottom // 垂直方向を下端に揃える
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray) // ラベル（項目名）
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium) // 値（データ）
    }
}

// ====================================================================
// 3. プレビュー
// 画面の見た目をIDEで確認するための設定
// ====================================================================

@Preview(showBackground = true) // プレビューアノテーション
@Composable
fun ProfileScreenPreview() {
    ProfileScreen() // プレビューとしてメイン画面を表示
}