package com.example.smartcloset_frontend.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.smartcloset_frontend.utils.saveBitmapAndGetUri
import com.example.smartcloset_frontend.viewmodel.AddItemViewModel
import com.example.smartcloset_frontend.viewmodel.MasterDataViewModel
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class ItemFormState(
    val itemName: String = "",
    val color: Int = 0,
    val pattern: Int = 0,
    val brand: String = "",
    val size: Int = 0,
    val category: Int = 0, // category_detail_id
    val imageUri: String = "",
    val material: String = "",
    val feature: String = "",
    val taste: String = "",
    val season: String = "",
    val purchaseDate: String = "",
    val price: Int = 0,
    val tags: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun ItemRegistrationScreen(
    navController: NavController,
    viewModel: AddItemViewModel
) {
    val context = LocalContext.current
    val masterDataViewModel: MasterDataViewModel = viewModel()
    // ViewModelの状態を初期値として使用（戻ってきた時に値が保持される）
    var itemState by remember { mutableStateOf(viewModel.itemState) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showSizeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showPatternDialog by remember { mutableStateOf(false) }
    
    // ViewModelの状態が変更されたらローカル状態も更新
    // ただし、ViewModelが空の状態（初期状態）の場合は更新しない（画面が一瞬見えるのを防ぐ）
    LaunchedEffect(viewModel.itemState) {
        val vmState = viewModel.itemState
        // 全てのフィールドが空の場合は更新しない（クリアされた状態）
        val isEmpty = vmState.itemName.isBlank() && 
                      vmState.imageUri.isBlank() && 
                      vmState.category == 0 && 
                      vmState.color == 0 && 
                      vmState.pattern == 0 && 
                      vmState.size == 0 && 
                      vmState.brand.isBlank() && 
                      vmState.material.isBlank() && 
                      vmState.feature.isBlank() && 
                      vmState.taste.isBlank() && 
                      vmState.season.isBlank()
        
        if (!isEmpty) {
            itemState = vmState
        }
    }

    // 1. ギャラリー画像選択ランチャー
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val newState = itemState.copy(imageUri = it.toString())
            itemState = newState
            viewModel.setFormState(newState)
        }
    }
    // Bitmapを保持するための状態を追加
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncherBitmap = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            capturedBitmap = it
            // 画像を保存してUriを取得する場合
            val uri = saveBitmapAndGetUri(context, it)
            val newState = itemState.copy(imageUri = uri.toString())
            itemState = newState
            viewModel.setFormState(newState)
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

            RegistrationForm(
                state = itemState,
                onStateChange = { 
                    itemState = it
                    // ViewModelにも反映（戻ってきた時に値が保持される）
                    viewModel.setFormState(it)
                },
                onCategoryClick = { showCategoryDialog = true },
                onSizeClick = { showSizeDialog = true },
                onColorClick = { showColorDialog = true },
                onPatternClick = { showPatternDialog = true },
                masterDataViewModel = masterDataViewModel
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    // バリデーション
                    val validationError = validateForm(itemState)
                    if (validationError != null) {
                        Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
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
                        val uri = createImageUri(context)
                        photoUri = uri
                        // カメラを起動
                        cameraLauncher.launch(uri)
                    }
                ) {
                    Text("カメラで撮影")
                }
            }
        )
    }

    // カテゴリー選択ダイアログ
    if (showCategoryDialog) {
        CategorySelectionDialog(
            selectedCategoryId = itemState.category,
            onCategorySelected = { categoryId ->
                val newState = itemState.copy(category = categoryId)
                itemState = newState
                viewModel.setFormState(newState)
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false },
            masterDataViewModel = masterDataViewModel
        )
    }

    // サイズ選択ダイアログ
    if (showSizeDialog) {
        SizeSelectionDialog(
            selectedSizeId = itemState.size,
            onSizeSelected = { sizeId ->
                val newState = itemState.copy(size = sizeId)
                itemState = newState
                viewModel.setFormState(newState)
                showSizeDialog = false
            },
            onDismiss = { showSizeDialog = false },
            masterDataViewModel = masterDataViewModel
        )
    }

    // カラー選択ダイアログ
    if (showColorDialog) {
        ColorSelectionDialog(
            selectedColorId = itemState.color,
            onColorSelected = { colorId ->
                val newState = itemState.copy(color = colorId)
                itemState = newState
                viewModel.setFormState(newState)
                showColorDialog = false
            },
            onDismiss = { showColorDialog = false },
            masterDataViewModel = masterDataViewModel
        )
    }

    // パターン選択ダイアログ
    if (showPatternDialog) {
        PatternSelectionDialog(
            selectedPatternId = itemState.pattern,
            onPatternSelected = { patternId ->
                val newState = itemState.copy(pattern = patternId)
                itemState = newState
                viewModel.setFormState(newState)
                showPatternDialog = false
            },
            onDismiss = { showPatternDialog = false },
            masterDataViewModel = masterDataViewModel
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
                painter = rememberAsyncImagePainter(model = Uri.parse(imageUri)),
                contentDescription = "Selected Image",
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
    onCategoryClick: () -> Unit,
    onSizeClick: () -> Unit,
    onColorClick: () -> Unit,
    onPatternClick: () -> Unit,
    masterDataViewModel: MasterDataViewModel
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
            value = masterDataViewModel.categoryMap[state.category] ?: "未選択",
            onClick = onCategoryClick
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SelectableField(
                label = "カラー",
                value = masterDataViewModel.colorMap[state.color] ?: "未選択",
                onClick = onColorClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            SelectableField(
                label = "パターン",
                value = masterDataViewModel.patternMap[state.pattern] ?: "未選択",
                onClick = onPatternClick,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RegistrationTextField(
                label = "ブランド",
                placeholder = "ブランド名",
                value = state.brand,
                onValueChange = { onStateChange(state.copy(brand = it)) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            SelectableField(
                label = "サイズ",
                value = masterDataViewModel.sizeMap[state.size] ?: "未選択",
                onClick = onSizeClick,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(16.dp))

        RegistrationTextField(
            label = "素材",
            placeholder = "素材名",
            value = state.material,
            onValueChange = { onStateChange(state.copy(material = it)) }
        )
        Spacer(Modifier.height(16.dp))

        RegistrationTextField(
            label = "特徴",
            placeholder = "特徴",
            value = state.feature,
            onValueChange = { onStateChange(state.copy(feature = it)) }
        )
        Spacer(Modifier.height(16.dp))

        RegistrationTextField(
            label = "テイスト",
            placeholder = "テイスト",
            value = state.taste,
            onValueChange = { onStateChange(state.copy(taste = it)) }
        )
        Spacer(Modifier.height(16.dp))

        RegistrationTextField(
            label = "シーズン",
            placeholder = "シーズン",
            value = state.season,
            onValueChange = { onStateChange(state.copy(season = it)) }
        )
    }
}
@Composable
fun RegistrationTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
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

@Composable
fun SelectableField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                color = if (value == "未選択") Color.Gray else Color.Black
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Select",
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
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

    Column {
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

        if (tags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    AssistChip(
                        onClick = { /* 削除など */ },
                        label = { Text(tag) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Remove",
                                Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySelectionDialog(
    selectedCategoryId: Int,
    onCategorySelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    masterDataViewModel: MasterDataViewModel
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("カテゴリーを選択") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                masterDataViewModel.categoryMap.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, modifier = Modifier.weight(1f))
                        if (selectedCategoryId == id) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "選択中",
                                tint = Color(0xFF6495ED)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
fun SizeSelectionDialog(
    selectedSizeId: Int,
    onSizeSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    masterDataViewModel: MasterDataViewModel
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("サイズを選択") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                masterDataViewModel.sizeMap.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSizeSelected(id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, modifier = Modifier.weight(1f))
                        if (selectedSizeId == id) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "選択中",
                                tint = Color(0xFF6495ED)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
fun ColorSelectionDialog(
    selectedColorId: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    masterDataViewModel: MasterDataViewModel
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("カラーを選択") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                masterDataViewModel.colorMap.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onColorSelected(id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, modifier = Modifier.weight(1f))
                        if (selectedColorId == id) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "選択中",
                                tint = Color(0xFF6495ED)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
fun PatternSelectionDialog(
    selectedPatternId: Int,
    onPatternSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    masterDataViewModel: MasterDataViewModel
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("パターンを選択") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                masterDataViewModel.patternMap.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPatternSelected(id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, modifier = Modifier.weight(1f))
                        if (selectedPatternId == id) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "選択中",
                                tint = Color(0xFF6495ED)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
fun DatePickerDialog(
    currentDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(java.util.Calendar.getInstance()) }
    
    // 現在の日付をパース
    if (currentDate.isNotEmpty()) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
            val date = sdf.parse(currentDate)
            if (date != null) {
                val cal = java.util.Calendar.getInstance()
                cal.time = date
                selectedDate = cal
            }
        } catch (e: Exception) {
            // パースエラーは無視
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("購入日を選択") },
        text = {
            Column {
                // 簡易的な日付選択（実際にはDatePickerを使うべき）
                Text("日付選択機能は実装が必要です")
                Text("現在の日付: ${currentDate.ifEmpty { "未設定" }}")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
                onDateSelected(sdf.format(selectedDate.time))
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

// バリデーション関数
fun validateForm(state: ItemFormState): String? {
    return when {
        state.imageUri.isBlank() -> "画像を選択してください"
        state.itemName.isBlank() -> "名称を入力してください"
        state.category == 0 -> "カテゴリーを選択してください"
        state.color == 0 -> "カラーを選択してください"
        state.pattern == 0 -> "パターンを選択してください"
        state.brand.isBlank() -> "ブランドを入力してください"
        state.size == 0 -> "サイズを選択してください"
        state.material.isBlank() -> "素材を入力してください"
        state.feature.isBlank() -> "特徴を入力してください"
        state.taste.isBlank() -> "テイストを入力してください"
        state.season.isBlank() -> "シーズンを入力してください"
        else -> null
    }
}
