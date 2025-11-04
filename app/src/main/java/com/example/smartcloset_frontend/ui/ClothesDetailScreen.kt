//package com.example.smartcloset_frontend.ui
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.Star
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavHostController
//import coil.compose.AsyncImage
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ClothesDetailScreen(
//    navController: NavHostController,
//    clothesId: String? = null
//) {
//    // TODO: 既存のRepository等からデータを取得
//    // 仮のデータ（後で実際のデータ取得に置き換え）
//    val clothesData = remember {
//        ClothesDetailData(
//            id = clothesId ?: "1",
//            name = "ウインドプルーフスタンドカラージャケット",
//            imageUrl = null, // 画像URLまたはnull
//            tags = listOf("アウター", "グレイ", "ブルゾン"),
//            brand = "ユニクロ",
//            size = "M",
//            purchaseDate = "2025/10/10",
//            price = "¥5,990"
//        )
//    }
//    var isFavorite by remember { mutableStateOf(false) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//            // ヘッダー
//            TopAppBar(
//                title = {
//                    Text(
//                        text = clothesData.name.takeIf { it.length <= 20 }
//                            ?: "${clothesData.name.take(17)}...",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black,
//                        maxLines = 1
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "戻る",
//                            tint = Color.Black
//                        )
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { isFavorite = !isFavorite }) {
//                        Icon(
//                            imageVector = Icons.Default.Star,
//                            contentDescription = "お気に入り",
//                            tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color.White
//                )
//            )
//
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 16.dp)
//                    .verticalScroll(rememberScrollState()),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // 商品画像
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(300.dp)
//                        .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (clothesData.imageUrl != null) {
//                        AsyncImage(
//                            model = clothesData.imageUrl,
//                            contentDescription = clothesData.name,
//                            modifier = Modifier.fillMaxSize()
//                        )
//                    } else {
//                        // プレースホルダー（グレーの背景）
//                        Text(
//                            text = "画像",
//                            color = Color.Gray,
//                            fontSize = 16.sp
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // タグ
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Start
//                ) {
//                    clothesData.tags.forEach { tag ->
//                        Box(
//                            modifier = Modifier
//                                .padding(end = 8.dp)
//                                .background(
//                                    Color(0xFFE0F7FA),
//                                    RoundedCornerShape(4.dp)
//                                )
//                                .padding(horizontal = 12.dp, vertical = 6.dp)
//                        ) {
//                            Text(
//                                text = tag,
//                                color = Color(0xFF2196F3),
//                                fontSize = 12.sp
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // 詳細情報
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(
//                        containerColor = Color.White
//                    ),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//                ) {
//                    Column {
//                        DetailRow("ブランド", clothesData.brand)
//                        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
//                        DetailRow("サイズ", clothesData.size)
//                        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
//                        DetailRow("購入日", clothesData.purchaseDate)
//                        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
//                        DetailRow("価格", clothesData.price)
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // アクションボタン
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    // 削除ボタン
//                    Button(
//                        onClick = {
//                            // TODO: 削除処理
//                        },
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(48.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFFE53935)
//                        )
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Delete,
//                            contentDescription = "削除",
//                            tint = Color.White
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = "削除",
//                            color = Color.White,
//                            fontSize = 14.sp
//                        )
//                    }
//
//                    // 編集ボタン
//                    Button(
//                        onClick = {
//                            // TODO: 編集処理
//                        },
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(48.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF2AFF33)
//                        )
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Edit,
//                            contentDescription = "編集",
//                            tint = Color.White
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = "編集",
//                            color = Color.White,
//                            fontSize = 14.sp
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//        }
//    }
//}
//
//@Composable
//fun DetailRow(label: String, value: String) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 12.dp, horizontal = 16.dp),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text(
//            text = label,
//            color = Color.Black,
//            fontSize = 14.sp
//        )
//        Text(
//            text = value,
//            color = Color.Black,
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Medium
//        )
//    }
//}
//
//// 仮のデータクラス（後で実際のClothesDataに置き換え）
//data class ClothesDetailData(
//    val id: String,
//    val name: String,
//    val imageUrl: String?,
//    val tags: List<String>,
//    val brand: String,
//    val size: String,
//    val purchaseDate: String,
//    val price: String
//)
//
