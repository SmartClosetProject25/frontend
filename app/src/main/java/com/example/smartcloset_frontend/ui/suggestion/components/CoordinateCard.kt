package com.example.smartcloset_frontend.ui.suggestion.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.example.smartcloset_frontend.data.JudgeRequestData
import com.example.smartcloset_frontend.data.Proposal
import com.example.smartcloset_frontend.ui.common.ImageGenerationHelper
import com.example.smartcloset_frontend.ui.common.ModelSelectionDialog
import com.example.smartcloset_frontend.utils.createImageFileUri
import com.example.smartcloset_frontend.viewmodel.ItemViewModel
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModel

/**
 * コーディネートカードコンポーネント
 */
@Composable
fun CoordinateCard(
    proposal: Proposal,
    suggestionViewModel: SuggestionViewModel,
    isGeneratingImage: Boolean,
    modifier: Modifier = Modifier,
    userSessionViewModel: UserSessionViewModel,
    itemViewModel: ItemViewModel
) {
    val userId by userSessionViewModel.userId.collectAsState()
    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }
    var isReasonExpanded by remember { mutableStateOf(false) }
    var showModelSelectionDialog by remember { mutableStateOf(false) }
    // カメラ撮影用の一時ファイルUriを保持
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    // アウター設定を保持（カメラ/アルバム選択時に使用）
    var selectedIsOuter by remember { mutableStateOf(true) }

    // カメラ撮影用のLauncher（高解像度で撮影）
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraImageUri != null) {
            showModelSelectionDialog = false
            cameraImageUri?.let { uri ->
                ImageGenerationHelper.startImageGeneration(
                    proposal = proposal,
                    modelBitmap = null,
                    modelUri = uri,
                    modelTemplate = null,
                    isOuter = selectedIsOuter,
                    context = context,
                    suggestionViewModel = suggestionViewModel
                )
            }
        }
        cameraImageUri = null
    }

    // カメラ権限リクエスト用
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageFileUri(context)
            if (uri != null) {
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "画像ファイルの作成に失敗しました", Toast.LENGTH_SHORT).show()
            }
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
                proposal = proposal,
                modelBitmap = null,
                modelUri = it,
                modelTemplate = null,
                isOuter = selectedIsOuter,
                context = context,
                suggestionViewModel = suggestionViewModel
            )
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ItemDisplay(proposal.items.outer, "アウター")
                ItemDisplay(proposal.items.tops, "インナー")
                ItemDisplay(proposal.items.bottoms, "ボトムス")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 理由をアコーディオン形式で表示
            if (proposal.reason.isNotBlank()) {
                // 展開ボタン
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isReasonExpanded = !isReasonExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "詳細を見る",
                        fontSize = 12.sp,
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (isReasonExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isReasonExpanded) "折りたたむ" else "展開する",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 理由のテキスト（アニメーション付き）
                AnimatedVisibility(
                    visible = isReasonExpanded,
                    enter = expandVertically(
                        animationSpec = tween(300),
                        expandFrom = Alignment.Top
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(300),
                        shrinkTowards = Alignment.Top
                    )
                ) {
                    Text(
                        text = proposal.reason,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            Divider(color = Color(0xFFE0E0E0))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左側：✨生成ボタン（大きめ）
                Button(
                    onClick = {
                        showModelSelectionDialog = true
                    },
                    enabled = !isGeneratingImage,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (isGeneratingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "生成",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "生成",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 右側：評価ボタン（👍と👎）
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 👍評価ボタン
                    IconButton(
                        onClick = {
                            isLiked = !isLiked
                            if (isLiked) isDisliked = false
                            val data = JudgeRequestData(
                                userId = userId,
                                planItemId = 2,
                                vote = "good"
                            )
                            itemViewModel.sendJudge(data)
                        }
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = if (isLiked) Color(0xFF2196F3) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 👎評価ボタン
                    IconButton(
                        onClick = {
                            isDisliked = !isDisliked
                            if (isDisliked) isLiked = false
                            val data = JudgeRequestData(
                                userId = userId,
                                planItemId = 2,
                                vote = "bad"
                            )
                            itemViewModel.sendJudge(data)
                        }
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier
                                .rotate(180f)
                                .size(24.dp),
                            tint = if (isDisliked) Color(0xFFE53935) else Color.Gray
                        )
                    }
                }
            }
        }
    }

    // モデル選択ダイアログ
    if (showModelSelectionDialog) {
        ModelSelectionDialog(
            onDismiss = {
                showModelSelectionDialog = false
            },
            onCameraClick = { isOuter ->
                selectedIsOuter = isOuter
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (granted) {
                    val uri = createImageFileUri(context)
                    if (uri != null) {
                        cameraImageUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        Toast.makeText(context, "画像ファイルの作成に失敗しました", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onGalleryClick = { isOuter ->
                selectedIsOuter = isOuter
                galleryLauncher.launch("image/*")
            },
            onMannequinClick = { isOuter ->
                showModelSelectionDialog = false
                ImageGenerationHelper.startImageGeneration(
                    proposal = proposal,
                    modelBitmap = null,
                    modelUri = null,
                    modelTemplate = "mannequin",
                    isOuter = isOuter,
                    context = context,
                    suggestionViewModel = suggestionViewModel
                )
            },
            onProfileClick = { isOuter ->
                showModelSelectionDialog = false
                ImageGenerationHelper.startImageGeneration(
                    proposal = proposal,
                    modelBitmap = null,
                    modelUri = null,
                    modelTemplate = "profile",
                    isOuter = isOuter,
                    context = context,
                    suggestionViewModel = suggestionViewModel
                )
            }
        )
    }
}