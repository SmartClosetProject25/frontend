//package com.example.smartcloset_frontend.ui
//
//// ----------------------------------------------------
//// ★ 以下のインポートが、エラー解消のために必須です ★
//// ----------------------------------------------------
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.layout.FlowRow // タグの折り返し用
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.Star
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
//
//// SmallTopAppBar の使用に必要
//import androidx.compose.material3.SmallTopAppBar
//import androidx.compose.material3.ExperimentalMaterial3Api
//// ----------------------------------------------------
//
//
//// ⚠️ 注意: DetailRow は CommonComposables.kt に定義されていることが前提です。
//
//// ==========================================
//// 👕 服詳細画面 (ClothesDetailsScreen)
//// ==========================================
//@OptIn(ExperimentalMaterial3Api::class) // SmallTopAppBar のために必要
//@Composable
//fun ClothesDetailsScreen(
//    navController: NavController
//) {
//    // プレースホルダーデータ
//    val itemName = "ウィンドプルーフスタンドカラ..."
//    val tags = listOf("アウター", "グレー", "ブルゾン")
//
//    Scaffold(
//        topBar = {
//            SmallTopAppBar( // 💡 これで関数が解決されるはず
//                title = {
//                    Text(
//                        text = itemName,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { /* お気に入りロジック */ }) {
//                        Icon(Icons.Filled.Star, contentDescription = "Favorite", tint = Color.LightGray)
//                    }
//                }
//            )
//        },
//        bottomBar = {
//            NavigationBar {
//                NavigationBarItem(selected = true, onClick = { /* home */ }, icon = { Icon(Icons.Filled.Star, contentDescription = "Home") }, label = { Text("Home") })
//                NavigationBarItem(selected = false, onClick = { /* settings */ }, icon = { Icon(Icons.Filled.Star, contentDescription = "Settings") }, label = { Text("Settings") })
//                NavigationBarItem(selected = false, onClick = { /* add */ }, icon = { Icon(Icons.Filled.Star, contentDescription = "Add") }, label = { Text("Add") })
//                NavigationBarItem(selected = false, onClick = { /* explore */ }, icon = { Icon(Icons.Filled.Star, contentDescription = "Explore") }, label = { Text("Explore") })
//                NavigationBarItem(selected = false, onClick = { /* profile */ }, icon = { Icon(Icons.Filled.Star, contentDescription = "Profile") }, label = { Text("Profile") })
//            }
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .verticalScroll(rememberScrollState()),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // --- 1. アイテム画像 (プレースホルダー) ---
//            Spacer(Modifier.height(8.dp))
//
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth(0.9f)
//                    .aspectRatio(1f)
//                    .clip(RoundedCornerShape(4.dp))
//                    .background(Color(0xFFE0E0E0))
//            ) { /* Placeholder Image */ }
//            Spacer(Modifier.height(16.dp))
//
//            // --- 2. タグエリア (AssistChip) ---
//            FlowRow(
//                modifier = Modifier
//                    .fillMaxWidth(0.95f)
//                    .padding(horizontal = 8.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                tags.forEach { tag ->
//                    AssistChip(
//                        onClick = { /* no op */ },
//                        label = { Text(tag) },
//                        colors = AssistChipDefaults.assistChipColors(
//                            containerColor = Color(0xFFE0E0E0),
//                            labelColor = Color.DarkGray
//                        )
//                    )
//                }
//            }
//            Spacer(Modifier.height(24.dp))
//
//            // --- 3. 詳細情報リスト ---
//            Column(modifier = Modifier.fillMaxWidth(0.9f).padding(horizontal = 8.dp)) {
//                DetailRow(label = "ブランド", value = "ユニクロ")
//                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
//                DetailRow(label = "サイズ", value = "M")
//                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
//                DetailRow(label = "購入日", value = "2025/10/10")
//                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
//                DetailRow(label = "価格", value = "¥5,990")
//            }
//
//            Spacer(Modifier.height(32.dp))
//
//            // --- 4. 削除/編集ボタンエリア ---
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth(0.9f)
//                    .padding(horizontal = 8.dp),
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                // 削除ボタン (赤系)
//                Button(
//                    onClick = { /* 削除ロジック */ },
//                    modifier = Modifier.weight(1f).height(50.dp),
//                    shape = RoundedCornerShape(8.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
//                ) {
//                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)
//                    Spacer(Modifier.width(4.dp))
//                    Text("削除", color = Color.White, fontWeight = FontWeight.Bold)
//                }
//
//                // 編集ボタン (緑系)
//                Button(
//                    onClick = { /* 編集画面へ遷移ロジック */ },
//                    modifier = Modifier.weight(1f).height(50.dp),
//                    shape = RoundedCornerShape(8.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF689F38))
//                ) {
//                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White)
//                    Spacer(Modifier.width(4.dp))
//                    Text("編集", color = Color.White, fontWeight = FontWeight.Bold)
//                }
//            }
//            Spacer(Modifier.height(16.dp))
//        }
//    }
//}
//
//// ==========================================
//// プレビュー
//// ==========================================
//@Preview(showBackground = true)
//@Composable
//fun PreviewClothesDetailsScreenPlaceholder() {
//    MaterialTheme {
//        ClothesDetailsScreen(navController = rememberNavController())
//    }
//}
