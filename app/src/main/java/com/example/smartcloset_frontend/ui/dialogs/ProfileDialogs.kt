package com.example.smartcloset_frontend.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/** 共通ヘッダー（色/アイコン/タイトル/サブタイトル/閉じる） */
@Composable
private fun StatusHeader(
    bg: Color,
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(bg, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterStart)) {
            icon()
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
            }
        }
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(Icons.Rounded.Close, contentDescription = "close", tint = Color.White)
        }
    }
}

/** 更新成功ポップアップ */
@Composable
fun ProfileUpdateSuccessDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 4.dp) {
            Column(Modifier.widthIn(min = 320.dp)) {
                StatusHeader(
                    bg = Color(0xFF3B82F6),
                    icon = { Icon(Icons.Rounded.Info, contentDescription = null, tint = Color.White) },
                    title = "更新が完了しました",
                    subtitle = "変更は正常に保存されました。",
                    onClose = onDismiss
                )
                Spacer(Modifier.height(40.dp)) // 本文エリア（空）
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp)
                ) { Text("確認") }
            }
        }
    }
}

/** 更新失敗ポップアップ */
@Composable
fun ProfileUpdateErrorDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 4.dp) {
            Column(Modifier.widthIn(min = 320.dp)) {
                StatusHeader(
                    bg = Color(0xFFEF4444),
                    icon = { Icon(Icons.Rounded.Info, contentDescription = null, tint = Color.White) },
                    title = "更新に失敗しました",
                    subtitle = "通信状況をご確認の上、時間をおいて再度お試しください。",
                    onClose = onDismiss
                )
                Spacer(Modifier.height(40.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp)
                ) { Text("確認") }
            }
        }
    }
}

/** 削除確認ポップアップ */
@Composable
fun ProfileDeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 4.dp) {
            Column(Modifier.widthIn(min = 320.dp)) {
                StatusHeader(
                    bg = Color(0xFFF59E0B),
                    icon = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color.White) },
                    title = "本当に削除しますか？",
                    subtitle = "この操作は元に戻せません。",
                    onClose = onDismiss
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("いいえ") }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("はい") }
                }
            }
        }
    }
}
