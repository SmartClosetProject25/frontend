package com.example.smartcloset_frontend.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage

import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.ui.networkErr.AsyncState
import com.example.smartcloset_frontend.viewmodel.ClothesDetailViewModel

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothesDetailScreen(
    navController: NavHostController,
    itemId: Int,
    viewModel: ClothesDetailViewModel = viewModel()
) {
    // ---- 詳細ロード ----
    LaunchedEffect(itemId) {
        viewModel.loadDetail(itemId)
    }

    val state = viewModel.detailState
    var isFavorite by remember { mutableStateOf(false) }

    when (state) {
        // --- ローディング中 ---
        AsyncState.Idle,
        AsyncState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // --- エラー ---
        is AsyncState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("詳細の取得に失敗しました", color = Color.Red)
            }
        }

        // --- 成功：ここでだけ詳細UIを描画 ---
        is AsyncState.Success -> {
            val clothesData = state.data   // ← ItemDetailData

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // ヘッダー
                    TopAppBar(
                        title = {
                            Text(
                                text = "商品詳細",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                maxLines = 1
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "戻る",
                                    tint = Color.Black
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { isFavorite = !isFavorite }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "お気に入り",
                                    tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White
                        )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // 商品画像
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val imageUrl =
                                if (clothesData?.imageUrl?.startsWith("http") ?: false) {
                                    clothesData.imageUrl
                                } else {
                                    BuildConfig.SERVER_URL.trimEnd('/') + clothesData?.imageUrl
                                }

                            AsyncImage(
                                model = imageUrl,
                                contentDescription = clothesData?.itemName,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Spacer(modifier = Modifier.height(24.dp))

                        // 詳細情報
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column {
                                DetailRow("ブランド", clothesData?.brandName ?: "不明")
                                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                                DetailRow("サイズ", clothesData?.size ?: "-")
                                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                                DetailRow("カテゴリ", clothesData?.category ?: "-")
                                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                                DetailRow("色", clothesData?.color ?: "-")
                                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                                DetailRow("素材", clothesData?.material ?: "-")
                                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                                DetailRow("特徴", clothesData?.feature ?: "-")
                                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                                DetailRow("シーズン", clothesData?.season ?: "-")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // アクションボタン
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 削除ボタン
                            Button(
                                onClick = {
                                    // TODO: 削除処理
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE53935)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "削除",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "削除",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }

                            // 編集ボタン
                            Button(
                                onClick = {
                                    // TODO: 編集処理
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2AFF33)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "編集",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "編集",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Bold)
        Text(value)
    }
}