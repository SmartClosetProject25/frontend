package com.example.smartcloset_frontend.ui

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
//import com.example.smartcloset_frontend.navigation.Screen
import com.example.smartcloset_frontend.ui.theme.SmartClosetTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Serializable //
data class ItemFormState(
    val itemName: String = "",
    val brandName: String? = null,
    val size: String? = null,
    val purchaseDate: String = "", // 例: "2025/10/10"
    val price: Int = 0,
    val category: String = "未選択",
    val tags: List<String> = emptyList(),
    val imageUri: String? = null // Uri.toString()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemRegistrationScreen(
    navController: NavController
) {
    // フォームの状態を保持 (ItemFormStateを参照)
    var itemState by remember { mutableStateOf(ItemFormState()) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            itemState = itemState.copy(imageUri = it.toString())
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
            ImageUploadArea(
                imageUri = itemState.imageUri,
                onImageSelect = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            RegistrationForm(
                state = itemState,
                onStateChange = { itemState = it },
                onTagAdded = { newTag ->
                    if (newTag.isNotBlank() && !itemState.tags.contains(newTag)) {
                        itemState = itemState.copy(tags = itemState.tags + newTag.trim())
                    }
                }
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    // データをJSONに変換し、ルートにエンコードして渡す
                    val itemJson = Json.encodeToString(itemState)
                    val encodedJson = URLEncoder.encode(itemJson, StandardCharsets.UTF_8.toString())
                    navController.navigate("confirmation/$encodedJson")
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
}

// ==========================================
// 画像アップロードエリア (ImageUploadArea)
// ==========================================
@Composable
fun ImageUploadArea(imageUri: String?, onImageSelect: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onImageSelect),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUri.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(imageUri)),
                contentDescription = "Selected Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Icon(
                Icons.Filled.Upload,
                contentDescription = "Upload Image",
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
        }
    }
}

// ==========================================
// 登録フォーム全体 (RegistrationForm)
// ==========================================
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

        SelectableField(label = "カテゴリー", value = state.category) { /* 選択ロジック */ }
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RegistrationTextField(
                label = "ブランド",
                placeholder = "ブランド名",
                value = state.brandName ?: "",
                onValueChange = { onStateChange(state.copy(brandName = it)) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            SelectableField(
                label = "サイズ", value = state.size ?: "", modifier = Modifier.weight(1f)
            ) { /* サイズ選択 */ }
        }
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DateField(
                label = "購入日", value = state.purchaseDate, modifier = Modifier.weight(1f)
            ) { /* DatePicker */ }
            Spacer(Modifier.width(16.dp))
            PriceField(
                label = "価格", value = state.price, modifier = Modifier.weight(1f),
                onValueChange = { if (it >= 0) onStateChange(state.copy(price = it)) }
            )
        }
        Spacer(Modifier.height(16.dp))

        TagFieldWithInput(
            tags = state.tags,
            onTagAdded = onTagAdded
        )
    }
}

// --- 補助 Composable (以降の関数は変更なし) ---

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
