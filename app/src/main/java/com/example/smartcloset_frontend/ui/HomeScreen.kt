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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

import androidx.navigation.NavHostController

import com.example.smartcloset_frontend.R
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.network.ServerUrlHolder
import com.example.smartcloset_frontend.viewmodel.ItemViewModel
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModel
import com.example.smartcloset_frontend.ui.networkErr.AsyncState


//const val baseUrl = BuildConfig.SERVER_URL
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: ItemViewModel,
    userSessionViewModel : UserSessionViewModel
) {
    val userId by userSessionViewModel.userId.collectAsState()
    
    // userIdが変更されたときにアイテムをクリアして再読み込み
    LaunchedEffect(userId) {
        val currentUserId = userId
        if (currentUserId == null) {
            // userIdがnullになった場合（ログアウト時など）はアイテムをクリア
            viewModel.clearItems()
        } else {
            // userIdが設定されたときは強制的に再読み込み（ユーザー切り替えを考慮）
            viewModel.loadItems(currentUserId, forceRefresh = true)
        }
    }
    
    // 画面が表示されたときにアイテム一覧を再読み込み（更新後の反映のため）
    // ただし、userIdが変更されたときはLaunchedEffectで処理されるので、ここではスキップ
    DisposableEffect(userId) {
        userId?.let { id ->
            // 少し遅延させて、初回読み込みが完了してから再読み込み
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                delay(300)
                viewModel.loadItems(id, forceRefresh = true)
            }
        }
        onDispose { }
    }

    val items = viewModel.items
    val isLoading = viewModel.isLoading
    val itemListState = viewModel.itemListState
    
//    val extendedItems = remember(items) {
//        if (items.isEmpty()) emptyList()
//        else List(20) { index -> items[index % items.size] }
//    }


    var selectedCategory by remember { mutableStateOf("すべて") }
    var searchText by remember { mutableStateOf("") }

    val categories = listOf("すべて", "トップス", "ジャケット・アウター", "パンツ", "スカート")

    val categoryMap = mapOf(
        1 to "トップス",
        2 to "ジャケット・アウター",
        3 to "パンツ",
        4 to "スカート"
    )
    val filteredItems = remember(items, selectedCategory, searchText) {
        val categoryId = when (selectedCategory) {
            "トップス" -> 1
            "ジャケット・アウター" -> 2
            "パンツ" -> 3
            "スカート" -> 4
            else -> null // "すべて"
        }

        items.filter { item ->
            // カテゴリ条件
            (categoryId == null || item.category == categoryId) &&
                    // 検索条件
                    (searchText.isBlank() ||
                            item.itemName.contains(searchText, ignoreCase = true))
        }
    }
    val extendedItems = remember(filteredItems) {
        if (filteredItems.isEmpty()) emptyList()
        else List(20) { index -> filteredItems[index % filteredItems.size] }
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 500)

    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
// お気に入り状態を保持するマップ
    val favorites = remember {
        mutableStateMapOf<Int, Boolean>()
    }
    // キーボードとフォーカスマネージャーの取得
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val onSearch: (String) -> Unit = { query ->
        searchText = query
        keyboardController?.hide()
        focusManager.clearFocus()
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
                    onSearch(searchText)
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
        
        // 読み込み中またはuserIdが設定される前はローディング表示
        if (isLoading || userId == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (items.isEmpty() && itemListState is AsyncState.Error) {
            // 読み込み完了後、エラー状態で、かつアイテムが空の場合のみエラー表示
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("アイテムを読み込めませんでした")
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    userId?.let { viewModel.loadItems(it, forceRefresh = true) }
                }) {
                    Text("再読み込み")
                }
            }
        } else if (items.isEmpty()) {
            // データが0件の場合（エラーではない）
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("アイテムがありません")
            }
        } else {
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
//                                val fullUrl = baseUrl + item.imageUrl
                                val fullUrl =
                                    (ServerUrlHolder.overrideBaseUrl ?: BuildConfig.SERVER_URL) + item.imageUrl

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
                                            val current = favorites[item.id] ?: false
                                            val newValue = !current

                                            // ローカル状態を更新
                                            favorites[item.id] = newValue

                                            // サーバーへ送信
                                            userId?.let { uid ->
                                                viewModel.toggleFavoriteOnServer(
                                                    userId = uid,
                                                    itemId = item.id,
                                                    isFavorite = newValue
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        val isFavorite = favorites[item.id] ?: false
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
        }

        // 今日のコーデボタン
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    navController.navigate("coordinate")
                },
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
