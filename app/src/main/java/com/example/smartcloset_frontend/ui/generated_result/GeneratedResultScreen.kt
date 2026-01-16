package com.example.smartcloset_frontend.ui.generated_result

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.smartcloset_frontend.ui.common.ImageGenerationHelper
import com.example.smartcloset_frontend.ui.common.ModelSelectionDialog
import com.example.smartcloset_frontend.ui.generated_result.components.CoordinateImageSection
import com.example.smartcloset_frontend.ui.generated_result.components.CoordinateTopBar
import com.example.smartcloset_frontend.ui.generated_result.components.ItemDetailCard
import com.example.smartcloset_frontend.ui.generated_result.components.QrCodeSection
import com.example.smartcloset_frontend.ui.generated_result.utils.UrlGenerator
import com.example.smartcloset_frontend.utils.QrCodeGenerator
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel

/**
 * 生成結果画面
 */
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
            ImageGenerationHelper.startImageGeneration(
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
            ImageGenerationHelper.startImageGeneration(
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
                val htmlPageUrl = UrlGenerator.generateHtmlPageUrl(imageUrl, coordinateId)
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
                            ImageGenerationHelper.startImageGeneration(
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
                            ImageGenerationHelper.startImageGeneration(
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
                ) {
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