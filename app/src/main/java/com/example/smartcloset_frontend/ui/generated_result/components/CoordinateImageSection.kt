package com.example.smartcloset_frontend.ui.generated_result.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcloset_frontend.data.Proposal
import com.example.smartcloset_frontend.ui.common.ImageUrlHelper
import com.example.smartcloset_frontend.ui.common.RetryableImage

/**
 * コーディネート画像セクション
 */
@Composable
fun CoordinateImageSection(
    imageUrlOrPath: String?,
    selectedProposal: Proposal? = null,
    coordinateId: Int? = null,
    isGeneratingImage: Boolean = false,
    onGenerateClick: (() -> Unit)? = null
) {
    val imageUrl = remember(imageUrlOrPath) {
        ImageUrlHelper.buildImageUrl(imageUrlOrPath)
    }

    // 画像がない場合、生成ボタンを表示
    if (imageUrl == null && !isGeneratingImage) {
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
        RetryableImage(
            imagePath = imageUrlOrPath,
            contentDescription = "生成されたコーディネート画像",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentScale = ContentScale.FillWidth
        )
    }
}