package com.example.smartcloset_frontend.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.smartcloset_frontend.viewmodel.AddItemViewModel
import kotlinx.serialization.Serializable

@Serializable
data class ItemFormState(
    val itemName: String = "",
    val color: Int = 0,
    val pattern: Int = 0,
    val brand: String = "",
    val size: Int = 0,
    val category: Int = 0,
    val imageUri: String = "",
    val material: String = "",
    val feature: String = "",
    val taste: String = "",
    val season: String = "",
)

val categoryMap = mapOf(
    0 to "未選択",
    1 to "トップス",
    2 to "ボトムス",
    3 to "アウター",
    4 to "アクセサリー"
)
val sizeMap = mapOf(
    0 to "xs",
    1 to "s",
    2 to "m",
    3 to "l",
    4 to "ll"
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemRegistrationScreen(
    navController: NavController,
    viewModel: AddItemViewModel = AddItemViewModel()
) {
    val context = LocalContext.current
    // フォームの状態を保持 (ItemFormStateを参照)
    var itemState by remember { mutableStateOf(ItemFormState()) }
    // 画像ソース選択ダイアログの表示状態
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // 1. ギャラリー画像選択ランチャー
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            itemState = itemState.copy(imageUri = it.toString())
        }
    }

    // 2. カメラ撮影ランチャー (Bitmapを返す)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            // BitmapをUriとして保存する処理は複雑なので、ここでは単純にComposable側でBitmapを扱うための
            // 一時的なUri変換またはViewModelでのデータ保持が必要です。
            // 簡易的にBitmapをそのまま表示できるように処理します。
            // 実際のアプリでは、Bitmapをファイルに保存し、そのUriをitemStateに設定する必要があります。
            // ここではデモとして、Bitmapが存在することを示すフラグ(または、後述の修正)を設定します。
            // ItemFormStateがUriを受け取る設計なので、ここではUriを保存する実装は省略し、
            // 成功したことだけを示すためにUri.EMPTY.toString()をセットします。（実際のアプリでは要修正）
            // 🚨 注意: TakePicturePreviewはサムネイルを返すため、高解像度画像が必要な場合は
            // FileProviderとTakePictureを使用する必要があります。
            // ItemFormStateを Bitmap/Uri のいずれかを受け取れるように修正するのが最も簡単ですが、
            // 今回は既存のItemFormStateに合わせて、Uriベースの画像を推奨します。

            // 簡易対応として、Bitmapを保持する状態を追加します。
            // この例では、高解像度を扱うため、TakePicture()とUriの使用を推奨します。

            // 以下のコードは、高解像度画像が必要な場合の一般的な実装のヒントです。
            // 現在のコードでは実行できないため、ここではシンプルなBitmap版を続行します。

            // *** ItemFormStateに直接Bitmapを保持するための状態を一時的に追加します ***
            // 実際のプロダクションコードでは、ファイルに保存し、そのURIを使用してください。
            // val imageUriFromBitmap = saveBitmapAndGetUri(context, it) // 外部関数
            // itemState = itemState.copy(imageUri = imageUriFromBitmap.toString())
        }
    }

    // 3. カメラ撮影後のBitmapを保持する状態 (UriではなくBitmapを使う場合の応急処置)
    // 実際のアプリでは、上記のようにUriを使う設計を維持すべきです。
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncherBitmap = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            capturedBitmap = it
            // UriではなくBitmapを保持していることを示すために、imageUriにはnullをセット (排他的に扱う)
            itemState = itemState.copy(imageUri = "")
        }
    }


    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("登録")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // ImageUploadAreaのクリック時にダイアログを表示するように修正
            ImageUploadArea(
                imageUri = itemState.imageUri,
                capturedBitmap = capturedBitmap, // Bitmapを渡す
                onImageSelect = { showImageSourceDialog = true }, // ダイアログ表示
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            // 一時的にコメントアウト
//            RegistrationForm(
//                state = itemState,
//                onStateChange = { itemState = it },
//                onTagAdded = { newTag ->
//                    if (newTag.isNotBlank() && !itemState.tags.contains(newTag)) {
//                        itemState = itemState.copy(tags = itemState.tags + newTag.trim())
//                    }
//                }
//            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.setFormState(itemState)
                    navController.navigate("item_confirm")
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6495ED))
            ) {
                Text("確認へ進む", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // 4. 画像ソース選択ダイアログ
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("画像ソースを選択") },
            text = { Text("ギャラリーから選択するか、カメラで撮影しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        // ギャラリーを開く
                        imagePicker.launch("image/*")
                        capturedBitmap = null // ギャラリー選択時はBitmapをクリア
                    }
                ) {
                    Text("ギャラリー")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        // カメラを起動
                        cameraLauncherBitmap.launch(null)
                    }
                ) {
                    Text("カメラで撮影")
                }
            }
        )
    }
}

// ==========================================
// 画像アップロードエリア (ImageUploadArea) - Bitmap対応を追加
// ==========================================
@Composable
fun ImageUploadArea(
    imageUri: String?,
    capturedBitmap: Bitmap?, // Bitmap引数を追加
    onImageSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onImageSelect),
        contentAlignment = Alignment.Center
    ) {
        if (capturedBitmap != null) {
            // 撮影したBitmapを表示
            Image(
                bitmap = capturedBitmap.asImageBitmap(),
                contentDescription = "Captured Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            )
        } else if (!imageUri.isNullOrBlank()) {
            // ギャラリーから選択したUri画像を以前の通り表示
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(imageUri)),
                contentDescription = "Selected Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            // 画像がない場合のデフォルトアイコン
            Icon(
                Icons.Filled.Upload,
                contentDescription = "Upload Image",
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
        }
    }
}

// --- RegistrationForm および補助 Composable (変更なし、省略) ---

@Composable
fun RegistrationForm(
    state: ItemFormState,
    onStateChange: (ItemFormState) -> Unit,
    onTagAdded: (String) -> Unit
) {
    Column {
        RegistrationTextField(
            label = "名称",
            placeholder = "商品名",
            value = state.itemName,
            onValueChange = { onStateChange(state.copy(itemName = it)) }
        )
        Spacer(Modifier.height(16.dp))

        SelectableField(
            label = "カテゴリー",
            value = categoryMap[state.category] ?: "未選択",
        ) { /* 選択ロジック */ }
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//            RegistrationTextField(
//                label = "ブランド",
//                placeholder = "ブランド名",
//                value = state.brandName ?: "",
//                onValueChange = { onStateChange(state.copy(brandName = it)) },
//                modifier = Modifier.weight(1f)
//            )
            Spacer(Modifier.width(16.dp))
            SelectableField(
                label = "サイズ", value = sizeMap[state.size]?: "未選択", modifier = Modifier.weight(1f)
            ) { /* サイズ選択 */ }
        }
        Spacer(Modifier.height(16.dp))

//        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//            DateField(
//                label = "購入日", value = state.purchaseDate, modifier = Modifier.weight(1f)
//            ) { /* DatePicker */ }
//            Spacer(Modifier.width(16.dp))
//            PriceField(
//                label = "価格", value = state.price, modifier = Modifier.weight(1f),
//                onValueChange = { if (it >= 0) onStateChange(state.copy(price = it)) }
//            )
//        }
        Spacer(Modifier.height(16.dp))

//        TagFieldWithInput(
//            tags = state.tags,
//            onTagAdded = onTagAdded
//        )
    }
}
@Composable
fun RegistrationTextField(
    label: String,
    placeholder: String,
    value: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (value != null) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFEEEEEE),
                    unfocusedContainerColor = Color(0xFFEEEEEE),
                    disabledContainerColor = Color(0xFFEEEEEE),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
fun SelectableField(
    label: String,
    value: Any,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            //Text(value, style = MaterialTheme.typography.bodyLarge)
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Select",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun DateField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(value, style = MaterialTheme.typography.bodyLarge)
            Icon(
                Icons.Filled.CalendarToday,
                contentDescription = "Date Picker",
                modifier = Modifier.size(20.dp),
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun PriceField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(value.toString(), style = MaterialTheme.typography.bodyLarge)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Increase Price",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onValueChange(value + 1) }
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Decrease Price",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { if (value > 0) onValueChange(value - 1) }
                )
            }
        }
    }
}

@Composable
fun TagFieldWithInput(tags: List<String>, onTagAdded: (String) -> Unit) {
    var tagInput by remember { mutableStateOf("") }

    OutlinedTextField(
        value = tagInput,
        onValueChange = { tagInput = it },
        placeholder = { Text("タグを追加 +") },
        leadingIcon = { Icon(Icons.Filled.Label, contentDescription = "Tag") },
        trailingIcon = {
            if (tagInput.isNotBlank()) {
                IconButton(onClick = {
                    onTagAdded(tagInput)
                    tagInput = ""
                }) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "Add Tag")
                }
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFEEEEEE),
            unfocusedContainerColor = Color(0xFFEEEEEE),
            disabledContainerColor = Color(0xFFEEEEEE),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(8.dp)
    )

    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            AssistChip(
                onClick = { /* 削除など */ },
                label = { Text(tag) },
                leadingIcon = { Icon(Icons.Filled.Close, contentDescription = "Remove", Modifier.size(16.dp)) }
            )
        }
    }
}


// ==========================================
// プレビュー
// ==========================================
@Preview(showBackground = true)
@Composable
fun PreviewItemRegistrationScreen() {
    // SmartClosetTheme 内でプレビュー
    // SmartClosetTheme {
    ItemRegistrationScreen(navController = rememberNavController())
    // }
}