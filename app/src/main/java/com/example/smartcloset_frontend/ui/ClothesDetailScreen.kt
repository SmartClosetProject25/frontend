package com.example.smartcloset_frontend.ui

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.network.ServerUrlHolder
import com.example.smartcloset_frontend.viewmodel.ClothesDetailViewModel
import com.example.smartcloset_frontend.viewmodel.MasterDataViewModel
import com.example.smartcloset_frontend.ui.networkErr.AsyncState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothesDetailScreen(
    navController: NavHostController,
    clothesId: String? = null
) {
    val context = LocalContext.current
    val detailViewModel: ClothesDetailViewModel = viewModel()
    val masterDataViewModel: MasterDataViewModel = viewModel()
    
    // clothesIdをIntに変換
    val itemId = clothesId?.toIntOrNull()
    
    // データの読み込み
    LaunchedEffect(itemId) {
        itemId?.let { id ->
            detailViewModel.loadDetail(id)
        }
    }
    
    val detailState = detailViewModel.detailState
    val itemDetail = when (val state = detailState) {
        is AsyncState.Success -> state.data
        else -> null
    }
    
    val isLoading = detailState is AsyncState.Loading
    val isError = detailState is AsyncState.Error
    
    // エラー処理
    LaunchedEffect(detailState) {
        if (isError) {
            val errorState = detailState as AsyncState.Error
            Toast.makeText(
                context,
                errorState.message ?: "データの取得に失敗しました",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    var isFavorite by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = itemDetail?.itemName?.takeIf { it.length <= 20 }
                            ?: itemDetail?.itemName?.take(17)?.plus("...")
                            ?: "アイテム詳細",
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
//                actions = {
//                    IconButton(onClick = { isFavorite = !isFavorite }) {
//                        Icon(
//                            imageVector = Icons.Default.Star,
//                            contentDescription = "お気に入り",
//                            tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray
//                        )
//                    }
//                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (itemDetail == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("データが見つかりませんでした", color = Color.Gray)
                }
            } else {
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
                            .fillMaxWidth(0.9f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!itemDetail.imageUrl.isNullOrBlank()) {
                            val fullUrl =
                                (ServerUrlHolder.overrideBaseUrl ?: BuildConfig.SERVER_URL) + itemDetail.imageUrl

                            Image(
                                painter = rememberAsyncImagePainter(fullUrl),
                                contentDescription = itemDetail.itemName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = "画像",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 詳細情報リスト（ItemConfirmScreenと同じ項目）
                    Column(modifier = Modifier.fillMaxWidth(0.9f).padding(horizontal = 8.dp)) {
                        // カテゴリー
                        DetailRow(
                            label = "カテゴリー",
                            value = masterDataViewModel.categoryMap[itemDetail.category] ?: "未選択"
                        )
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // カラー
                        DetailRow(
                            label = "カラー",
                            value = masterDataViewModel.colorMap[itemDetail.color] ?: "未選択"
                        )
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // パターン
                        DetailRow(
                            label = "パターン",
                            value = masterDataViewModel.patternMap[itemDetail.pattern] ?: "未選択"
                        )
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // ブランド
                        DetailRow(
                            label = "ブランド",
                            value = itemDetail.brandName ?: "未入力"
                        )
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // サイズ
                        DetailRow(
                            label = "サイズ",
                            value = masterDataViewModel.sizeMap[itemDetail.size] ?: "未選択"
                        )
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // 素材
                        DetailRowMultiLine(
                            label = "素材",
                            value = itemDetail.material ?: "未入力"
                        )
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // 特徴
                        DetailRowMultiLine(
                            label = "特徴",
                            value = itemDetail.feature ?: "未入力"
                        )
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // テイスト
                        DetailRowMultiLine(
                            label = "テイスト",
                            value = itemDetail.taste ?: "未入力"
                        )
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // シーズン
                        DetailRowMultiLine(
                            label = "シーズン",
                            value = itemDetail.season ?: "未入力"
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // アクションボタン
                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f),
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
                                itemDetail?.id?.let { id ->
                                    navController.navigate("item_edit/$id")
                                }
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
