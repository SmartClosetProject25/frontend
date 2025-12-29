package com.example.smartcloset_frontend.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import coil.compose.AsyncImagePainter
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.utils.QrCodeGenerator
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel

@Composable
fun GeneratedResultScreen(
    navController: NavHostController,
    suggestionViewModel: SuggestionViewModel
) {
    val generatedImageUrl by suggestionViewModel.generatedImage.collectAsState()
    val selectedProposal by suggestionViewModel.selectedProposal.collectAsState()
    val todayPlan by suggestionViewModel.todayPlan.collectAsState()
    var qrCodeBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    // QRコードを生成
    LaunchedEffect(generatedImageUrl) {
        generatedImageUrl?.let { url ->
            qrCodeBitmap = QrCodeGenerator.generateQrCode(url).asImageBitmap()
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
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
            ) {
                CoordinateImageSection(
                    imageUrlOrPath = generatedImageUrl
                )

                CoordinateSummarySection(
                    title = "今日のコーデ",
                    description = todayPlan?.plan ?: ""
                )

                Spacer(modifier = Modifier.height(24.dp))

                selectedProposal?.items?.outer?.item_name?.let {
                    ItemDetailSection(
                        category = "アウター",
                        name = it
                    )
                }
                selectedProposal?.items?.tops?.item_name?.let {
                    ItemDetailSection(
                        category = "トップス",
                        name = it
                    )
                }
                selectedProposal?.items?.bottoms?.item_name?.let {
                    ItemDetailSection(
                        category = "ボトムス",
                        name = it
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // QRコードセクション
                QrCodeSection(qrCodeBitmap = qrCodeBitmap)
                
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
    imageUrlOrPath: String?
) {
    val imageUrl: String? = imageUrlOrPath?.let { path ->
        if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            val baseUrl = BuildConfig.SERVER_URL.trimEnd('/')
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

    if (imageUrlWithRetry == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        SubcomposeAsyncImage(
            model = imageUrlWithRetry,
            contentDescription = "生成されたコーディネート画像",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "画像の読み込みに失敗しました",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = error?.message ?: "不明なエラー",
                            color = Color.Red,
                            fontSize = 12.sp
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
fun CoordinateSummarySection(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ItemDetailSection(category: String, name: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = category,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
    }
    Divider(
        color = Color.LightGray.copy(alpha = 0.5f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun QrCodeSection(qrCodeBitmap: ImageBitmap?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "QRコード",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        qrCodeBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "QRコード",
                modifier = Modifier
                    .size(200.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "このQRコードをスキャンして\n画像を表示できます",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        } ?: CircularProgressIndicator(
            modifier = Modifier.size(200.dp)
        )
    }
}
