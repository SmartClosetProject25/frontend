package com.example.smartcloset_frontend.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.smartcloset_frontend.R
import com.example.smartcloset_frontend.viewmodel.ProfileEditViewModel

// プロフィール編集画面のUIを定義するメインのComposable関数
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(navController: NavHostController, profileEditViewModel: ProfileEditViewModel = viewModel()) {
    // --- 状態管理 ---
    // rememberとmutableStateOfを使い、UIの状態を保持・監視する
    var name by remember { mutableStateOf("はるたろう") } // 名前
    var gender by remember { mutableStateOf("男") } // 性別
    val genderOptions = listOf("男", "女", "その他") // 性別の選択肢
    var height by remember { mutableStateOf("165") } // 身長
    var weight by remember { mutableStateOf("52") } // 体重
    var personalColor by remember { mutableStateOf("イエロー") } // パーソナルカラー
    val personalColorOptions = listOf("イエベ春", "ブルベ夏", "イエベ秋", "ブルベ冬") // パーソナルカラーの選択肢
    var skeleton by remember { mutableStateOf("ストレート") } // 骨格
    val skeletonOptions = listOf("ストレート", "ウェーブ", "ナチュラル") // 骨格の選択肢
    var imageUri by remember { mutableStateOf<Uri?>(null) } // 選択された画像のURI

    // 画像ギャラリーを起動し、選択結果を受け取るためのランチャー
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri } // 結果をimageUri状態にセット
    )

    // Scaffold: TopAppBar, BottomBar, Drawerなど基本的な画面構造を簡単に実装できるコンポーネント
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("編集") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // クリックで前の画面に戻る
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffoldが管理するパディングを適用
                .padding(horizontal = 24.dp) // 画面左右の余白
                .verticalScroll(rememberScrollState()), // 縦スクロールを可能にする
            horizontalAlignment = Alignment.CenterHorizontally // 子要素を水平方向中央に配置
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            // アバター画像
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape) // 円形に切り抜く
                    .background(Color(0xFFEFEFEF))
                    .clickable { imagePickerLauncher.launch("image/*") }, // クリックで画像ピッカーを起動
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    // Coilライブラリを使い、選択された画像を非同期で表示
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "選択されたアバター",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop // アスペクト比を維持したまま切り抜き
                    )
                } else {
                    // 画像が選択されていない場合のデフォルト表示
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF888888)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "アバターを編集",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // --- 入力フォーム ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp) // 各フォーム要素の間に16dpのスペースを設ける
            ) {
                // 1行目: 名前と性別
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                    FormTextField(label = "名前", value = name, onValueChange = { name = it }, modifier = Modifier.weight(1.5f))
                    FormSelectionField(label = "性別", value = gender, options = genderOptions, onValueSelected = { gender = it }, modifier = Modifier.weight(1f))
                }
                // 2行目: 身長と体重
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                    FormNumberField(label = "身長cm", value = height, onValueChange = { height = it }, modifier = Modifier.weight(1f))
                    FormNumberField(label = "体重kg", value = weight, onValueChange = { weight = it }, modifier = Modifier.weight(1f))
                }
                // 3行目: パーソナルカラーと骨格
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                    FormSelectionField(label = "パーソナルカラー", value = personalColor, options = personalColorOptions, onValueSelected = { personalColor = it }, modifier = Modifier.weight(1f))
                    FormSelectionField(label = "骨格", value = skeleton, options = skeletonOptions, onValueSelected = { skeleton = it }, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = true)) // ボタンを画面下部に押しやるためのスペーサー

            // --- ボタン ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { navController.popBackStack() }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) { Text("キャンセル", color = Color.DarkGray) }
                Button(
                    onClick = {
                        // ViewModelの関数を呼び出し、現在のフォームデータをサーバーに送信
                        profileEditViewModel.updateProfile(name, gender, height, weight, personalColor, skeleton)
                        navController.popBackStack() // 送信後、前の画面に戻る
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF448AFF))
                ) { Text("更新", color = Color.White) }
            }
        }
    }
}

// テキスト入力用のカスタムコンポーザブル
@Composable
private fun FormTextField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value, // 表示する値
            onValueChange = onValueChange, // 値が変更されたときのコールバック
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF0F0F0),
                focusedContainerColor = Color(0xFFF0F0F0),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            singleLine = true // 入力を1行に制限
        )
    }
}

// ドロップダウン選択用のカスタムコンポーザブル
@Composable
private fun FormSelectionField(label: String, value: String, options: List<String>, onValueSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) } // ドロップダウンメニューの開閉状態

    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF0F0F0),
                onClick = { expanded = true } // クリックでメニューを開く
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(value, style = MaterialTheme.typography.bodyLarge)
                    Icon(painter = painterResource(id = R.drawable.chevronright), contentDescription = null, tint = Color.Gray)
                }
            }
            // ドロップダウンメニュー本体
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }, // メニュー外のクリックで閉じる
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueSelected(option) // 選択された値をコールバックで通知
                            expanded = false // メニューを閉じる
                        }
                    )
                }
            }
        }
    }
}

// 数値入力用のカスタムコンポーザブル
@Composable
private fun FormNumberField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val currentValue = value.toIntOrNull() ?: 0 // 文字列を数値に変換。失敗した場合は0

    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF0F0F0)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(value, style = MaterialTheme.typography.bodyLarge)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 上矢印：クリックで数値を1増やす
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Up",
                        modifier = Modifier.size(18.dp).rotate(180f).clickable { onValueChange((currentValue + 1).toString()) },
                        tint = Color.Gray
                    )
                    // 下矢印：クリックで数値を1減らす
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Down",
                        modifier = Modifier.size(18.dp).clickable { onValueChange((currentValue - 1).toString()) },
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

// プレビュー用の設定
@Preview(showBackground = true)
@Composable
fun ProfileEditScreenPreview() {
    ProfileEditScreen(navController = rememberNavController())
}
