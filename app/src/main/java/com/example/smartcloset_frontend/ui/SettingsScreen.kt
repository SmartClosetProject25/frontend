package com.example.smartcloset_frontend.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    var pushNotifications by remember { mutableStateOf(false) }
    var codeSuggestions by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(true) }

    var locationUsage by remember { mutableStateOf(false) }
    var aiDataUsage by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 通知設定
            item { SectionTitle("通知設定") }

            item {
                SettingSwitchItem("プッシュ通知", pushNotifications) {
                    pushNotifications = it
                }
            }

            item {
                SettingSwitchItem("コーデを提案", codeSuggestions) {
                    codeSuggestions = it
                }
            }

            item {
                SettingSwitchItem("メッセージ", messages) {
                    messages = it
                }
            }

            // プライバシー設定
            item { SectionTitle("プライバシー設定") }

            item {
                SettingArrowItem("個人情報の取り扱いについて") { /* navigate */ }
            }

            item {
                SettingSwitchItem("位置情報の利用", locationUsage) {
                    locationUsage = it
                }
            }

            item {
                SettingSwitchItem("AI学習へのデータ提供", aiDataUsage) {
                    aiDataUsage = it
                }
            }

            // プライバシー設定（再掲：利用ガイドなど）
            item { SectionTitle("プライバシー設定") }

            item {
                SettingArrowItem("利用ガイド") { /* navigate */ }
            }

            item {
                SettingArrowItem("よくある質問") { /* navigate */ }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingArrowItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )
        Icon(Icons.Default.ArrowForward, contentDescription = null)
    }
}
