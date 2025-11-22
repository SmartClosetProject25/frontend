package com.example.smartcloset_frontend.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smartcloset_frontend.ui.dialogs.*
import java.io.File

// 保存されている画像枚数を数えるユーティリティ関数
fun countItemImages(context: Context): Int {
    val dir = File(context.filesDir, "ItemImgs")
    if (!dir.exists()) return 0

    // jpg / png などの画像だけを数える
    val files = dir.listFiles { file ->
        file.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp")
    }

    return files?.size ?: 0
}
// 保存されている画像をすべて削除するユーティリティ関数
fun deleteAllItemImages(context: Context): Int {
    val dir = File(context.filesDir, "ItemImgs")
    if (!dir.exists()) return 0

    val files = dir.listFiles() ?: return 0

    var deletedCount = 0
    for (file in files) {
        if (file.isFile && file.delete()) {
            deletedCount++
        }
    }
    return deletedCount
}


@Composable
fun TestScreen(navController: NavHostController) {

    val context = LocalContext.current

    // ダイアログ表示用フラグ
    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TestSection(title = "画像処理用") {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val deleted = deleteAllItemImages(context)
                        Toast.makeText(
                            context,
                            "画像を${deleted}枚削除しました",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("全画像を削除")
                }
                Text(text = "保存されている画像枚数: ${countItemImages(context)}")
            }
        }
        // ナビゲーションセクション
        TestSection(
            title = "ナビゲーション",
            content = {
                Button(
                    onClick = {
                        navController.navigate("clothes_detail")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("服詳細画面へ移動")
                }
            }
        )

        // ダイアログテストセクション
        TestSection(
            title = "ダイアログテスト",
            content = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { showSuccess = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Success Dialog")
                    }
                    Button(
                        onClick = { showError = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Error Dialog")
                    }
                    Button(
                        onClick = { showDelete = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Delete Dialog")
                    }
                }
            }
        )
    }

    // 成功ダイアログ
    if (showSuccess) {
        ProfileUpdateSuccessDialog(onDismiss = { showSuccess = false })
    }

    // 失敗ダイアログ
    if (showError) {
        ProfileUpdateErrorDialog(onDismiss = { showError = false })
    }

    // 削除確認ダイアログ
    if (showDelete) {
        ProfileDeleteConfirmDialog(
            onConfirm = {
                // TODO: 削除処理をここに書く
                showDelete = false
            },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
fun TestSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

