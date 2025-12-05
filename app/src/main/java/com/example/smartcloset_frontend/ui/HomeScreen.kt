package com.example.smartcloset_frontend.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

import androidx.navigation.NavController

import com.example.smartcloset_frontend.R
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.viewmodel.ItemViewModel


const val baseUrl = BuildConfig.SERVER_URL
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ItemViewModel
) {
    // アイテム一覧の読み込み
    LaunchedEffect(Unit) {
        viewModel.loadItems(userId = 1) // TODO: 実ユーザーIDに
    }

    val items = viewModel.items

    val extendedItems = remember(items) {
        if (items.isEmpty()) emptyList()
        else List(20) { index -> items[index % items.size] }
    }

    var selectedCategory by remember { mutableStateOf("すべて") }
    var searchText by remember { mutableStateOf("") }

    val categories = listOf("すべて", "トップス", "ジャケット・アウター", "パンツ", "スカート")

    val categoryMap = mapOf(
        1 to "トップス",
        2 to "ジャケット・アウター",
        3 to "パンツ",
        4 to "スカート"
    )
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 500)

    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
// お気に入り状態を保持するマップ
    val favorites = remember {
        mutableStateMapOf<Int, Boolean>()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // 検索バー
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("アイテムを検索", fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.search_icon),
                    contentDescription = "検索",
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    println("検索実行：$searchText")
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // カテゴリープルダウン
        var expanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(selectedCategory)
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(id = R.drawable.arrow_icon),
                    contentDescription = "Dropdown",
                    modifier = Modifier.size(24.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // カード一覧
        LazyRow(
            state = listState,
            flingBehavior = flingBehavior, // ← スナップ動作を追加
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f) // ← 高さを圧迫しないように調整
        ) {
            items(extendedItems.size) { index ->
                Card(
                    modifier = Modifier
                        .width(320.dp) // ← サイズ調整
                        .height(460.dp) // ← サイズ調整で下ボタンが見えるように
                        .clickable {
                            navController.navigate("detail/${extendedItems[index].id}" )
                        },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    // 画像領域
                    Column(modifier = Modifier.padding(16.dp)) {

                        val item = extendedItems[index]
                        if (item.imageUrl != null) {
                            val fullUrl = baseUrl + item.imageUrl
                            Image(
                                painter = coil.compose.rememberAsyncImagePainter(fullUrl),

                                contentDescription = item.itemName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp)
                                    .background(Color.LightGray)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.itemName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1
                        )

                        Text(
                            text = "カテゴリ: ${categoryMap[item.category] ?: "不明"}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        // アイコン右下配置
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, end = 4.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = {
                                        val current = favorites[index] ?: false
                                        favorites[index] = !current
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    val isFavorite = favorites[index] ?: false
                                    Icon(
                                        painter = painterResource(
                                            id = if (isFavorite) R.drawable.star_filled_icon else R.drawable.star_empty_icon
                                        ),
                                        contentDescription = "お気に入り",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }


                                IconButton(
                                    onClick = {
//                                        TODO 直す
                                        val item = extendedItems[index]
                                        navController.navigate("detail/${item.id}")
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.edit_icon),
                                        contentDescription = "編集",
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 今日のコーデボタン
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBFD8FF)),
                modifier = Modifier
                    .wrapContentWidth()
                    .height(42.dp)
            ) {
                Text("今日のコーデを提案してもらう", color = Color(0xFF1C3E77))
            }
        }
    }
}
