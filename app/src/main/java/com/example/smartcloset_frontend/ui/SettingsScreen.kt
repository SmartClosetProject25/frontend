package com.example.smartcloset_frontend.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.data.PreferencesManager
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModel
import com.example.smartcloset_frontend.network.RetrofitClient
import com.example.smartcloset_frontend.network.ServerUrlHolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    navController: NavHostController? = null,
    userSessionViewModel: UserSessionViewModel
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    
    var pushNotifications by remember { mutableStateOf(false) }
    var codeSuggestions by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(true) }

    var locationUsage by remember { mutableStateOf(false) }
    var aiDataUsage by remember { mutableStateOf(true) }

    var aiSuggestionEnabled by remember { mutableStateOf(true) }
    var aiImageGenerationEnabled by remember { mutableStateOf(true) }

    var serverUrlInput by remember { mutableStateOf("") }
    var serverUrlMessage by remember { mutableStateOf<String?>(null) }
    var showServerForm by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getTestFlags()
            if (response.isSuccessful) {
                response.body()?.let { flags ->
                    aiImageGenerationEnabled = flags.enableAiImage
                    aiSuggestionEnabled = flags.enableAiSuggest
                }
            }
        } catch (e: Exception) {
            // 取得に失敗した場合はデフォルト値（どちらもON）のままにする
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
//                    }
//                }
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
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showServerForm = !showServerForm }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "サーバーURL設定",
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = if (showServerForm) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
                        contentDescription = null
                    )
                }
            }


            // ログアウト項目
            item { Spacer(modifier = Modifier.height(32.dp)) }
            
            item {
                LogoutItem(
                    onClick = {
                        userSessionViewModel.clear()
                        preferencesManager.clearLoginInfo()
                        RetrofitClient.clearCookies() // Cookieをクリア
                        navController?.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }



            // ===== サーバーURL設定（デバッグ用）=====
            if (showServerForm){
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "サーバーURL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text("例: http://192.168.50.77:5000/")
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val input = serverUrlInput.trim()
                                ServerUrlHolder.overrideBaseUrl =
                                    if (input.isBlank()) null
                                    else if (input.endsWith("/")) input else "$input/"

                                serverUrlMessage =
                                    if (input.isBlank()) "デフォルトに戻しました"
                                    else "サーバーURLを適用しました"
                            }
                        ) {
                            Text("適用")
                        }

                        OutlinedButton(
                            onClick = {
                                serverUrlInput = ""
                                ServerUrlHolder.overrideBaseUrl = null
                                serverUrlMessage = "デフォルトに戻しました"
                            }
                        ) {
                            Text("デフォルト")
                        }
                    }

                    serverUrlMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, fontSize = 12.sp)
                    }
                }
            }
            }

            item {
                SettingSwitchItem("AI提案", aiSuggestionEnabled) { newValue ->
                    aiSuggestionEnabled = newValue
                    coroutineScope.launch {
                        try {
                            val response = RetrofitClient.instance.setTestFlags(
                                enableAiImage = aiImageGenerationEnabled,
                                enableAiSuggest = newValue
                            )
                            if (response.isSuccessful) {
                                response.body()?.let { flags ->
                                    aiImageGenerationEnabled = flags.enableAiImage
                                    aiSuggestionEnabled = flags.enableAiSuggest
                                }
                            }
                        } catch (e: Exception) {
                            // エラー時はUI状態のみ変更し、サーバーエラーは無視する
                        }
                    }
                }
            }

            item {
                SettingSwitchItem("AI画像生成", aiImageGenerationEnabled) { newValue ->
                    aiImageGenerationEnabled = newValue
                    coroutineScope.launch {
                        try {
                            val response = RetrofitClient.instance.setTestFlags(
                                enableAiImage = newValue,
                                enableAiSuggest = aiSuggestionEnabled
                            )
                            if (response.isSuccessful) {
                                response.body()?.let { flags ->
                                    aiImageGenerationEnabled = flags.enableAiImage
                                    aiSuggestionEnabled = flags.enableAiSuggest
                                }
                            }
                        } catch (e: Exception) {
                            // エラー時はUI状態のみ変更し、サーバーエラーは無視する
                        }
                    }
                }
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

@Composable
fun LogoutItem(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ログアウト",
            fontSize = 16.sp,
            color = Color.Red,
            fontWeight = FontWeight.Medium
        )
    }
}
