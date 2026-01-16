package com.example.smartcloset_frontend.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.data.Item
import com.example.smartcloset_frontend.data.Proposal
import com.example.smartcloset_frontend.network.ServerUrlHolder
import com.example.smartcloset_frontend.utils.ImageUtils
import com.example.smartcloset_frontend.utils.QrCodeGenerator
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel
import java.net.URLEncoder

@Composable
fun GeneratedResultScreen(
    navController: NavHostController,
    suggestionViewModel: SuggestionViewModel
) {
    val context = LocalContext.current
    val generatedImageUrl by suggestionViewModel.generatedImage.collectAsState()
    val selectedProposal by suggestionViewModel.selectedProposal.collectAsState()
    val todayPlan by suggestionViewModel.todayPlan.collectAsState()
    val coordinateId by suggestionViewModel.coordinateId.collectAsState()
    val isGeneratingImage by suggestionViewModel.isGeneratingImage.collectAsState()
    var qrCodeBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var qrCodeError by remember { mutableStateOf<String?>(null) }
    var showModelSelectionDialog by remember { mutableStateOf(false) }
    
    // カメラ撮影用のLauncher
    val cameraLauncherBitmap = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            showModelSelectionDialog = false
            // カメラで撮影した画像で生成を開始
            startImageGenerationFromHistory(
                proposal = selectedProposal,
                coordinateId = coordinateId,
                modelBitmap = it,
                modelUri = null,
                modelTemplate = null,
                context = context,
                suggestionViewModel = suggestionViewModel
            )
        }
    }
    
    // カメラ権限リクエスト用
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncherBitmap.launch(null)
        } else {
            Toast.makeText(context, "カメラの権限が必要です", Toast.LENGTH_SHORT).show()
        }
    }

    // アルバムから画像選択用のLauncher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            showModelSelectionDialog = false
            // アルバムから選択した画像で生成を開始
            startImageGenerationFromHistory(
                proposal = selectedProposal,
                coordinateId = coordinateId,
                modelBitmap = null,
                modelUri = it,
                modelTemplate = null,
                context = context,
                suggestionViewModel = suggestionViewModel
            )
        }
    }
    
    // QRコードを生成（HTMLページのURLを使用）
    LaunchedEffect(generatedImageUrl, coordinateId) {
        generatedImageUrl?.let { imageUrl ->
            qrCodeError = null
            try {
                // HTMLページのURLを生成
                val htmlPageUrl = generateHtmlPageUrl(imageUrl, coordinateId)
                Log.d("GeneratedResultScreen", "QRコード生成URL: $htmlPageUrl")
                qrCodeBitmap = QrCodeGenerator.generateQrCode(htmlPageUrl).asImageBitmap()
            } catch (e: Exception) {
                Log.e("GeneratedResultScreen", "QRコード生成エラー: ${e.message}", e)
                qrCodeBitmap = null
                qrCodeError = e.message ?: "QRコードの生成に失敗しました"
            }
        } ?: run {
            qrCodeBitmap = null
            qrCodeError = null
        }
    }

    Scaffold(
        topBar = {
            CoordinateTopBar(title = "生成結果") {
                navController.popBackStack()
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // 生成画像セクション（カード形式）
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    CoordinateImageSection(
                        imageUrlOrPath = generatedImageUrl,
                        selectedProposal = selectedProposal,
                        coordinateId = coordinateId,
                        isGeneratingImage = isGeneratingImage,
                        onGenerateClick = {
                            showModelSelectionDialog = true
                        }
                    )
                }
                
                // モデル選択ダイアログ
                if (showModelSelectionDialog) {
                    ModelSelectionDialog(
                        onDismiss = {
                            showModelSelectionDialog = false
                        },
                        onCameraClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            if (granted) {
                                cameraLauncherBitmap.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        onGalleryClick = {
                            galleryLauncher.launch("image/*")
                        },
                        onMannequinClick = {
                            showModelSelectionDialog = false
                            startImageGenerationFromHistory(
                                proposal = selectedProposal,
                                coordinateId = coordinateId,
                                modelBitmap = null,
                                modelUri = null,
                                modelTemplate = "mannequin",
                                context = context,
                                suggestionViewModel = suggestionViewModel
                            )
                        },
                        onProfileClick = {
                            showModelSelectionDialog = false
                            startImageGenerationFromHistory(
                                proposal = selectedProposal,
                                coordinateId = coordinateId,
                                modelBitmap = null,
                                modelUri = null,
                                modelTemplate = "profile",
                                context = context,
                                suggestionViewModel = suggestionViewModel
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // コーディネート情報カード
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "今日のコーデ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 予定
                        if (!todayPlan?.plan.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Event,
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = todayPlan?.plan ?: "",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // アイテム詳細カード
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "使用アイテム",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        selectedProposal?.items?.outer?.let { item ->
                            ItemDetailCard(
                                category = "アウター",
                                item = item
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        selectedProposal?.items?.tops?.let { item ->
                            ItemDetailCard(
                                category = "トップス",
                                item = item
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        selectedProposal?.items?.bottoms?.let { item ->
                            ItemDetailCard(
                                category = "ボトムス",
                                item = item
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // QRコードセクション（カード形式）
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                )                 {
                    QrCodeSection(qrCodeBitmap = qrCodeBitmap, errorMessage = qrCodeError)
                }
                
                // コーディネート理由
                selectedProposal?.reason?.takeIf { it.isNotBlank() }?.let { reason ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "コーディネートのポイント",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = reason,
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    )
}

@Composable
fun CoordinateTopBar(title: String, onBackClicked: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "戻る",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onBackClicked)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
    }
}

@Composable
fun CoordinateImageSection(
    imageUrlOrPath: String?,
    selectedProposal: Proposal? = null,
    coordinateId: Int? = null,
    isGeneratingImage: Boolean = false,
    onGenerateClick: (() -> Unit)? = null
) {
    val imageUrl: String? = imageUrlOrPath?.let { path ->
        if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            val baseUrl = (ServerUrlHolder.overrideBaseUrl ?: BuildConfig.SERVER_URL).trimEnd('/')
            val imagePath = if (path.startsWith("/")) path else "/$path"
            "$baseUrl$imagePath"
        }
    }

    // リトライ用のキー
    var retryKey by remember { mutableStateOf(0) }
    val imageUrlWithRetry = remember(imageUrl, retryKey) {
        imageUrl?.let { url ->
            if (retryKey > 0) {
                val separator = if (url.contains("?")) "&" else "?"
                "$url${separator}_retry=$retryKey"
            } else {
                url
            }
        }
    }

    // 画像がない場合、生成ボタンを表示
    if (imageUrlWithRetry == null && !isGeneratingImage) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = null,
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "画像が生成されていません",
                    fontSize = 16.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ボタンをタップして画像を生成できます",
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E),
                    lineHeight = 20.sp
                )
                if (selectedProposal != null && coordinateId != null && onGenerateClick != null) {
                    Button(
                        onClick = onGenerateClick,
                        modifier = Modifier.padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("画像を生成する", fontSize = 16.sp)
                    }
                } else {
                    Text(
                        text = "コーディネート情報が不足しています",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }
    } else if (isGeneratingImage) {
        // 生成中
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color(0xFF2196F3)
                )
                Text(
                    text = "画像を生成中...",
                    fontSize = 16.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "しばらくお待ちください",
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    } else {
        SubcomposeAsyncImage(
            model = imageUrlWithRetry,
            contentDescription = "生成されたコーディネート画像",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentScale = ContentScale.FillWidth,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            error = { state ->
                val error = state.result.throwable
                Log.e("GeneratedResultScreen", "画像読み込みエラー: ${error?.message}", error)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "画像の読み込みに失敗しました",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = { retryKey++ },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("再試行")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ItemDetailCard(category: String, item: Item) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // アイテム画像
        val imageUrl = if (item.image_path.startsWith("http://") || item.image_path.startsWith("https://")) {
            item.image_path
        } else {
            val baseUrl = BuildConfig.SERVER_URL.trimEnd('/')
            val imagePath = if (item.image_path.startsWith("/")) item.image_path else "/${item.image_path}"
            "$baseUrl$imagePath"
        }
        
        // リトライ用のキー
        var retryKey by remember { mutableStateOf(0) }
        val imageUrlWithRetry = remember(imageUrl, retryKey) {
            imageUrl?.let { url ->
                if (retryKey > 0) {
                    val separator = if (url.contains("?")) "&" else "?"
                    "$url${separator}_retry=$retryKey"
                } else {
                    url
                }
            }
        }
        
        SubcomposeAsyncImage(
            model = imageUrlWithRetry,
            contentDescription = item.item_name,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE0E0E0))
                .clickable { retryKey++ },
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            },
            error = { state ->
                val error = state.result.throwable
                Log.e("GeneratedResultScreen", "アイテム画像読み込みエラー: ${error?.message}", error)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFFE0E0E0))
                        .clickable { retryKey++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "再読み込み",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // アイテム情報
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.item_name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            // タグ表示
            if (item.taste.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item.taste.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 画像URLからHTMLページのURLを生成する関数
 * @param imageUrl 画像のURL（相対パスまたは完全なURL）
 * @param coordinateId コーディネートID（オプション）
 * @return HTMLページの完全なURL
 */
fun generateHtmlPageUrl(imageUrl: String, coordinateId: Int?): String {
    // ベースURLの前後のスペースと末尾のスラッシュを削除
    val baseUrl = BuildConfig.SERVER_URL.trim().trimEnd('/')
    
    if (baseUrl.isBlank()) {
        throw IllegalArgumentException("SERVER_URLが設定されていません")
    }
    
    // 画像URLを完全なURLに変換
    val fullImageUrl = if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
        imageUrl.trim()
    } else {
        val imagePath = if (imageUrl.startsWith("/")) imageUrl else "/$imageUrl"
        "$baseUrl$imagePath"
    }
    
    // URLエンコード
    val encodedImageUrl = URLEncoder.encode(fullImageUrl, "UTF-8")
    
    // HTMLページのURLを生成
    // coordinateIdがある場合はそれを使用、ない場合はimage_urlパラメータを使用
    return if (coordinateId != null) {
        "$baseUrl/view_coordinate?coordinate_id=$coordinateId"
    } else {
        "$baseUrl/view_coordinate?image_url=$encodedImageUrl"
    }
}

@Composable
fun QrCodeSection(qrCodeBitmap: ImageBitmap?, errorMessage: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                Icons.Default.QrCode,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "QRコード",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        when {
            qrCodeBitmap != null -> {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrCodeBitmap,
                        contentDescription = "QRコード",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "このQRコードをスキャンして\n画像を表示できます",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = errorMessage,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// 履歴から画像を生成する関数
private fun startImageGenerationFromHistory(
    proposal: Proposal?,
    coordinateId: Int?,
    modelBitmap: Bitmap?,
    modelUri: Uri?,
    modelTemplate: String?,
    context: android.content.Context,
    suggestionViewModel: SuggestionViewModel
) {
    if (proposal == null || coordinateId == null) {
        Log.e("GeneratedResultScreen", "生成に必要な情報が不足しています")
        return
    }
    
    // proposal.itemsから各アイテムのimage_pathを取得
    val imagePaths = mutableListOf<String>()
    imagePaths.addAll(
        listOfNotNull(
            proposal.items.outer?.image_path,
            proposal.items.tops?.image_path,
            proposal.items.bottoms?.image_path
        )
    )
    
    if (imagePaths.isEmpty()) {
        Log.e("GeneratedResultScreen", "画像パスが取得できませんでした")
        return
    }
    
    // モデル画像をbase64エンコード（カメラ撮影時のみ）
    val modelImageBase64: String? = when {
        modelBitmap != null -> ImageUtils.bitmapToBase64(modelBitmap)
        modelUri != null -> ImageUtils.uriToBase64(context, modelUri)
        else -> null
    }
    
    // 画像生成を実行（バックグラウンド処理に対応）
    suggestionViewModel.generateImage(
        context = context,
        imagePaths = imagePaths,
        modelImageBase64 = modelImageBase64,
        modelTemplate = modelTemplate,
        proposal = proposal,
        coordinateId = coordinateId,
        useBackgroundGeneration = true
    )
}
