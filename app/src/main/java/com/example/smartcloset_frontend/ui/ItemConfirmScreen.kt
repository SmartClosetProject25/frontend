package com.example.smartcloset_frontend.ui

// ItemFormState が定義されているファイルはインポートを省略

// [注] ItemFormStateはItemFormState.ktに定義されていると仮定し、
// ここではItemFormStateがアクセス可能であることを前提とします。
// 以前のファイルで定義されていた ItemFormState の定義を仮に利用します。
// 実際にはItemFormState.ktファイルに定義されているはずです。
import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.smartcloset_frontend.utils.saveImageToLocalItemFolder
import com.example.smartcloset_frontend.viewmodel.AddItemViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.example.smartcloset_frontend.ui.networkErr.AsyncState

// ==========================================
// アイテム詳細確認画面
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemConfirmationScreen(
    navController: NavController,
    viewModel: AddItemViewModel
) {
    val context = LocalContext.current
    val itemState = viewModel.itemState
    val addItemState = viewModel.addItemState

    // 成功・失敗を一度だけ処理したいので LaunchedEffect を使う
    LaunchedEffect(addItemState) {
        when (val state = addItemState) {
            is AsyncState.Success<*> -> {
                // ★ 本当に Success 状態になったときだけ成功トースト&遷移
                Toast.makeText(context, "登録が完了しました", Toast.LENGTH_SHORT).show()
                viewModel.resetAddItemState()
                navController.navigate("home") {
                    popUpTo("item_confirm") { inclusive = true }
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

                        IconButton(onClick = { /* お気に入り登録ロジック */ }) {
                            Icon(Icons.Filled.Star, contentDescription = "Favorite", tint = Color.LightGray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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

            Image(
                painter = rememberAsyncImagePainter(model = imageUri),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
//                itemState.tags.forEach { tag ->
//                    AssistChip(
//                        onClick = { /* no op */ },
//                        label = { Text(tag) },
//                        colors = AssistChipDefaults.assistChipColors(
//                            containerColor = Color(0xFFE0E0E0),
//                            labelColor = Color.DarkGray
//                        )
//                    )
//                }
            }
            Spacer(Modifier.height(24.dp))

            // 詳細情報リスト
            Column(modifier = Modifier.fillMaxWidth(0.9f).padding(horizontal = 8.dp)) {
                // ブランド
                DetailRow(label = "ブランド", value = itemState.brand)
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                // サイズ
                DetailRow(label = "サイズ", value = itemState.size)
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                // 購入日
                //DetailRow(label = "購入日", value = itemState.purchaseDate)
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                // 価格
                //DetailRow(label = "価格", value = "¥${itemState.price}")
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
                        val uri = itemState.imageUri
                        if (uri.isBlank()) {
                            Toast.makeText(context, "画像が設定されていません", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.addItem(uri.toUri(), userId = 1)
                    },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6495ED))
                ) {
//                    Text("登録", color = Color.White)
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("登録中…")
                    } else {
                        Text("この内容で登録")
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
fun DetailRow(label: String, value: Any) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
        Text(
            text = categoryMap[value] ?: "未選択",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
