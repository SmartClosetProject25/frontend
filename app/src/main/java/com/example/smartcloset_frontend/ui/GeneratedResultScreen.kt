package com.example.smartcloset_frontend.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController // ⭐ NavHostController を使用

// GeneratedResultScreen の画面本体 (元の CoordinateScreen に NavController を追加)
@Composable
fun GeneratedResultScreen(navController: NavHostController) { // ⭐ NavHostController を引数に追加
    Scaffold(
        topBar = {
            CoordinateTopBar(title = "生成結果") {
                // ⭐ 戻るボタンの処理: ナビゲーションバックを実行
                navController.popBackStack()
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                // 1. コーディネート画像とアクションボタン
                CoordinateImageSection(
                    onShareClicked = { /* 共有処理 */ },
                    onLikeClicked = { /* いいね処理 */ },
                    onDislikeClicked = { /* いまいち処理 */ }
                )

                // 2. コーディネート概要
                CoordinateSummarySection(
                    title = "今日のコーデ",
                    description = "友達とごはん"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. アイテム詳細
                ItemDetailSection(
                    category = "アウター",
                    name = "ウィンドプルーフスタンドブルゾン"
                )
                ItemDetailSection(
                    category = "トップス",
                    name = "スウェットシャツ"
                )
                ItemDetailSection(
                    category = "ボトムス",
                    name = "スウェットワイドパンツ"
                )
            }
        }
    )
}

// 1. トップバー
@Composable
fun CoordinateTopBar(title: String, onBackClicked: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "戻る",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onBackClicked)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
    }
}

// 2. 画像セクション
@Composable
fun CoordinateImageSection(
    onShareClicked: () -> Unit,
    onLikeClicked: () -> Unit,
    onDislikeClicked: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 画像コンポーネントのプレースホルダー
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.LightGray)
            ) {
                Text("コーディネート画像", modifier = Modifier.align(Alignment.Center))
            }

            // 共有ボタン (右上)
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "共有",
                tint = Color.Black,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(8.dp)
                    .clickable(onClick = onShareClicked)
            )
        }

        // いいね/いまいちボタン (右下)
        Row(
            modifier = Modifier
                .align(Alignment.End)
                .offset(y = (-16).dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // いいねボタン
            Icon(
                imageVector = Icons.Default.ThumbUp,
                contentDescription = "いいね",
                tint = Color.White,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF4285F4))
                    .padding(8.dp)
                    .clickable(onClick = onLikeClicked)
            )
            // いまいちボタン
            Icon(
                imageVector = Icons.Default.ThumbDown,
                contentDescription = "いまいち",
                tint = Color.White,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFEA4335))
                    .padding(8.dp)
                    .clickable(onClick = onDislikeClicked)
            )
        }
    }
}

// 3. コーディネート概要セクション
@Composable
fun CoordinateSummarySection(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

// 4. アイテム詳細表示 (カテゴリと商品名)
@Composable
fun ItemDetailSection(category: String, name: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = category,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
    }
    // アイテムごとに区切り線を入れる場合
    Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}