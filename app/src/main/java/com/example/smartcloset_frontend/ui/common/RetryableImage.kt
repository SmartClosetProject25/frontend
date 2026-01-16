package com.example.smartcloset_frontend.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import coil.compose.SubcomposeAsyncImage
import com.example.smartcloset_frontend.ui.common.ImageUrlHelper.addRetryParameter

/**
 * リトライ機能付きの画像コンポーネント
 * @param imagePath 画像のパス（相対パスまたは完全なURL）
 * @param contentDescription 画像の説明
 * @param modifier Modifier
 * @param contentScale 画像のスケール
 * @param onErrorClick エラー時のクリックハンドラ（nullの場合は自動リトライ）
 */
@Composable
fun RetryableImage(
    imagePath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onErrorClick: (() -> Unit)? = null
) {
    val imageUrl = remember(imagePath) {
        ImageUrlHelper.buildImageUrl(imagePath)
    }
    
    var retryKey by remember { mutableStateOf(0) }
    val imageUrlWithRetry = remember(imageUrl, retryKey) {
        imageUrl?.let { addRetryParameter(it, retryKey) }
    }
    
    SubcomposeAsyncImage(
        model = imageUrlWithRetry,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        },
        error = { state ->
            val error = state.result.throwable
            Log.e("RetryableImage", "画像読み込みエラー: ${error?.message}", error)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (onErrorClick != null) {
                            Modifier.clickable(onClick = onErrorClick)
                        } else {
                            Modifier.clickable { retryKey++ }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "タップして再読み込み",
                        color = Color.Gray,
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
}