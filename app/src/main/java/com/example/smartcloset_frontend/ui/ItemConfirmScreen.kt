package com.example.smartcloset_frontend.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.smartcloset_frontend.viewmodel.AddItemViewModel
import com.example.smartcloset_frontend.viewmodel.MasterDataViewModel
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.ui.networkErr.AsyncState
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState

// ==========================================
// アイテム詳細確認画面
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemConfirmationScreen(
    navController: NavController,
    viewModel: AddItemViewModel,
    userSessionViewModel: UserSessionViewModel,
    isEditMode: Boolean = false
) {
    val context = LocalContext.current
    val masterDataViewModel: MasterDataViewModel = viewModel()
    val userId by userSessionViewModel.userId.collectAsState()
    val itemState = viewModel.itemState
    val addItemState = viewModel.addItemState

    // 成功・失敗を一度だけ処理したいので LaunchedEffect を使う
    LaunchedEffect(addItemState) {
        when (val state = addItemState) {
            is AsyncState.Success<*> -> {
                // ★ 本当に Success 状態になったときだけ成功トースト&遷移
                val message = if (isEditMode) "更新が完了しました" else "登録が完了しました"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.resetAddItemState()
                // 遷移前にフォーム状態をクリア（遷移アニメーション中にクリアされる）
                viewModel.resetFormState()
                val popUpRoute = if (isEditMode) "item_confirm_edit" else "item_confirm"
                navController.navigate("home") {
                    popUpTo(popUpRoute) { inclusive = true }
                }
            }
            is AsyncState.Error -> {
                // ★ 失敗時はこっち。成功トーストは出ない
                Toast.makeText(
                    context,
                    state.message ?: "エラーが発生しました",
                    Toast.LENGTH_SHORT
                ).show()

                if (state.isNetworkError) {
                    // 必要ならネットワークエラー用画面に飛ばす
                    // navController.navigate("network_error") { ... }
                }

                viewModel.resetAddItemState()
            }
            else -> Unit
        }
    }

    val isLoading = addItemState is AsyncState.Loading


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 戻るボタンとスペースを揃えるためのダミーアイコン (必要に応じて削除)
                        IconButton(onClick = { /* no op for space */ }, enabled = false) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.Transparent)
                        }

                        Text(
                            text = itemState.itemName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

//                        IconButton(onClick = { /* お気に入り登録ロジック */ }) {
//                            Icon(Icons.Filled.Star, contentDescription = "Favorite", tint = Color.LightGray)
//                        }
                    }
                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // アイテム画像
            Spacer(Modifier.height(8.dp))
            // itemState.imageUriがnullでない場合のみtoUriを呼び出す
            val imageUri = itemState.imageUri
            
            // 画像URLを処理（サーバー上のURLの場合はbaseUrlを付ける）
            val imageModel = remember(imageUri) {
                when {
                    imageUri.isBlank() -> null
                    imageUri.startsWith("http://") || imageUri.startsWith("https://") -> imageUri
                    imageUri.startsWith("/static/") -> {
                        // サーバー上のURLの場合はbaseUrlを付ける
                        BuildConfig.SERVER_URL + imageUri
                    }
                    else -> imageUri // ローカルのUri（content://など）
                }
            }

            Image(
                painter = rememberAsyncImagePainter(
                    model = imageModel ?: ""
                ),
                contentDescription = "Item Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)) // 画像がない場合の背景色
            )
            Spacer(Modifier.height(16.dp))

            // タグエリア
            if (itemState.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemState.tags.forEach { tag ->
                        AssistChip(
                            onClick = { /* no op */ },
                            label = { Text(tag) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFFE0E0E0),
                                labelColor = Color.DarkGray
                            )
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 詳細情報リスト
            Column(modifier = Modifier.fillMaxWidth(0.9f).padding(horizontal = 8.dp)) {
                // カテゴリー
                DetailRow(label = "カテゴリー", value = masterDataViewModel.categoryMap[itemState.category] ?: "未選択")
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                
                // カラー
                DetailRow(label = "カラー", value = masterDataViewModel.colorMap[itemState.color] ?: "未選択")
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                
                // パターン
                DetailRow(label = "パターン", value = masterDataViewModel.patternMap[itemState.pattern] ?: "未選択")
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                
                // ブランド
                DetailRow(label = "ブランド", value = itemState.brand.ifEmpty { "未入力" })
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                
                // サイズ
                DetailRow(label = "サイズ", value = masterDataViewModel.sizeMap[itemState.size] ?: "未選択")
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                
                // 素材
                DetailRowMultiLine(label = "素材", value = itemState.material.ifEmpty { "未入力" })
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                
                // 特徴
                DetailRowMultiLine(label = "特徴", value = itemState.feature.ifEmpty { "未入力" })
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                
                // テイスト
                DetailRowMultiLine(label = "テイスト", value = itemState.taste.ifEmpty { "未入力" })
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                
                // シーズン
                DetailRowMultiLine(label = "シーズン", value = itemState.season.ifEmpty { "未入力" })
            }

            Spacer(Modifier.height(32.dp))

            // ボタンエリア
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 戻るボタン
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0C0C0))
                ) {
                    Text("戻る", color = Color.White)
                }
                // 登録ボタン
                        Button(
                            onClick = {
                                userId?.let { id ->
                                    if (isEditMode) {
                                        val uri = itemState.imageUri
                                        // サーバー上のURL（/static/で始まる、またはhttp:///https://で始まる）の場合は画像を更新しない
                                        val imageUri = when {
                                            uri.isBlank() -> null
                                            uri.startsWith("/static/") -> null // 既存のサーバー上の画像
                                            uri.startsWith("http://") || uri.startsWith("https://") -> null // 既存のサーバー上の画像
                                            else -> {
                                                // ローカルのUri（content://、file://など）の場合のみ変換
                                                try {
                                                    uri.toUri()
                                                } catch (e: Exception) {
                                                    null
                                                }
                                            }
                                        }
                                        viewModel.updateItem(imageUri, userId = id)
                                    } else {
                                        val uri = itemState.imageUri
                                        if (uri.isBlank()) {
                                            Toast.makeText(context, "画像が設定されていません", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        viewModel.addItem(uri.toUri(), userId = id)
                                    }
                                } ?: run {
                                    Toast.makeText(context, "ユーザーIDが取得できませんでした", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6495ED))
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(if (isEditMode) "更新中…" else "登録中…")
                            } else {
                                Text(if (isEditMode) "この内容で更新" else "この内容で登録")
                            }
                        }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ==========================================
// 補助 Composable: 詳細情報の行
// ==========================================
@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (value == "未選択" || value == "未入力") Color.Gray else Color.Black
        )
    }
}

@Composable
fun DetailRowMultiLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.DarkGray,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (value == "未選択" || value == "未入力") Color.Gray else Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}
