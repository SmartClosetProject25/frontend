package com.example.smartcloset_frontend.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.smartcloset_frontend.BuildConfig
import com.example.smartcloset_frontend.utils.QrCodeGenerator
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel

@Composable
fun GeneratedResultScreen(
    navController: NavHostController,
    suggestionViewModel: SuggestionViewModel
) {
    val generatedImageUrl by suggestionViewModel.generatedImage.collectAsState()
    var showQrCodeDialog by remember { mutableStateOf(false) }
    var qrCodeBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

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
                    imageUrlOrPath = generatedImageUrl,
                    onShareClicked = {
                        generatedImageUrl?.let {
                            qrCodeBitmap = QrCodeGenerator.generateQrCode(it).asImageBitmap()
                            showQrCodeDialog = true
                        }
                    }
                )
                
                if (showQrCodeDialog) {
                    QrCodeDialog(
                        qrCodeBitmap = qrCodeBitmap,
                        onDismiss = { showQrCodeDialog = false }
                    )
                }

                CoordinateSummarySection(
                    title = "今日のコーデ",
                    description = "友達とごはん"
                )

                Spacer(modifier = Modifier.height(24.dp))

                ItemDetailSection(
                    category = "アウター",
                    name = "ウィンドプルーフスタンドブルゾン"
                )
                ItemDetailSection(
                    category = "トップス",
                    name = "スウェットシャツ"
                )
                ItemDetailSection(
                    category = "ボトムス",
                    name = "スウェットワイドパンツ"
                )
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
    onShareClicked: () -> Unit
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

    // エラー時のリトライ用に、URLにタイムスタンプを追加して再試行を促す
    var retryKey by remember { mutableStateOf(0) }
    val imageUrlWithRetry = remember(imageUrl, retryKey) {
        imageUrl?.let { url ->
            // リトライ時はクエリパラメータを追加してURLを変更し、再読み込みを促す
            if (retryKey > 0) {
                val separator = if (url.contains("?")) "&" else "?"
                "$url${separator}_retry=$retryKey"
            } else {
                url
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (imageUrl == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            var imageState by remember { mutableStateOf<AsyncImagePainter.State?>(null) }
            
            Box(modifier = Modifier.fillMaxWidth()) {
                SubcomposeAsyncImage(
                    model = imageUrlWithRetry,
                    contentDescription = "生成されたコーディネート画像",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                ) {
                    imageState = painter.state
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            // ローディング中はプログレスインジケーターを表示
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is AsyncImagePainter.State.Error -> {
                            // エラー時でも画像を表示しようと試みる（読み込めたら自動的に表示される）
                            // 背景にプレースホルダーを表示
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "画像を読み込み中...",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            // エラー状態でも画像を表示しようと試みる
                            SubcomposeAsyncImageContent()
                        }
                        else -> {
                            SubcomposeAsyncImageContent()
                        }
                    }
                }
            }
            
            // エラー状態の時、定期的にリトライする
            LaunchedEffect(imageState) {
                if (imageState is AsyncImagePainter.State.Error) {
                    delay(2000) // 2秒待機
                    retryKey++ // リトライキーを更新して再読み込みを促す
                }
            }
        }

        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "共有",
            tint = Color.Black,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(8.dp)
                .clickable(onClick = onShareClicked)
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
fun QrCodeDialog(
    qrCodeBitmap: ImageBitmap?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "QRコード",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                qrCodeBitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "QRコード",
                        modifier = Modifier
                            .size(300.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color.White)
                    )
                    Text(
                        text = "このQRコードをスキャンして\n画像を表示できます",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                } ?: Text("QRコードの生成に失敗しました")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}
