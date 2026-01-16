package com.example.smartcloset_frontend.ui.suggestion_history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smartcloset_frontend.data.CoordinateData
import com.example.smartcloset_frontend.ui.common.ImagePreloader
import com.example.smartcloset_frontend.ui.common.ImageUrlHelper
import com.example.smartcloset_frontend.ui.suggestion_history.HistoryDateGroup
import com.example.smartcloset_frontend.ui.suggestion_history.HistoryCoordinate
import com.example.smartcloset_frontend.ui.suggestion_history.utils.navigateToGeneratedResult
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel

/**
 * 日付グループセクションコンポーネント
 */
@Composable
fun DateGroupSection(
    dateGroup: HistoryDateGroup,
    index: Int,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    scrollState: ScrollState,
    screenHeight: Float,
    navController: NavHostController,
    suggestionViewModel: SuggestionViewModel,
    coordinateDataMap: Map<Int, CoordinateData>
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // セクションの位置を追跡
    var sectionTopY by remember { mutableStateOf<Float?>(null) }
    var sectionHeight by remember { mutableStateOf<Float?>(null) }

    // 画像プリロード状態
    var imagesPreloaded by remember(dateGroup, isExpanded) { mutableStateOf(false) }
    var shouldPreload by remember { mutableStateOf(false) }

    // すべての画像URLを収集（展開されている場合のみ）
    val imageUrls = remember(dateGroup, isExpanded) {
        if (!isExpanded) {
            emptyList()
        } else {
            val paths = mutableListOf<String>()
            dateGroup.suggestions.forEach { suggestion ->
                // アイテム画像
                listOfNotNull(
                    suggestion.outer?.image_path,
                    suggestion.top.image_path,
                    suggestion.bottom.image_path
                ).forEach { paths.add(it) }
                // 生成画像
                suggestion.genimgPath?.let { paths.add(it) }
            }
            ImageUrlHelper.buildImageUrls(paths)
        }
    }

    // スクロール位置を監視して可視領域を判定（展開されている場合のみ）
    LaunchedEffect(scrollState.value, sectionTopY, sectionHeight, isExpanded) {
        if (!isExpanded) {
            shouldPreload = false
            return@LaunchedEffect
        }

        val scrollY = with(density) { scrollState.value.toFloat() }
        val viewportBottom = scrollY + screenHeight

        // 一番新しい日付（index == 0）は即座にプリロード
        if (index == 0) {
            shouldPreload = true
        } else if (sectionTopY != null && sectionHeight != null) {
            // セクションが表示領域に入ったか判定（少し前からプリロード開始）
            val preloadThreshold = screenHeight * 0.5f // 画面の50%手前からプリロード
            val sectionBottom = sectionTopY!! + sectionHeight!!
            shouldPreload = sectionTopY!! < viewportBottom + preloadThreshold
        }
    }

    // 画像をプリロードする（展開されている場合のみ）
    LaunchedEffect(dateGroup, shouldPreload, isExpanded) {
        if (isExpanded && shouldPreload && imageUrls.isNotEmpty() && !imagesPreloaded) {
            ImagePreloader.preloadImages(context, imageUrls)
            imagesPreloaded = true
        } else if (!isExpanded) {
            // 折りたたまれた場合はプリロード状態をリセット
            imagesPreloaded = false
        }
    }

    Column(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            val position = coordinates.positionInRoot()
            sectionTopY = position.y
            sectionHeight = coordinates.size.height.toFloat()
        }
    ) {
        // 日付ヘッダー（クリック可能）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!isExpanded) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateGroup.date,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(8.dp))

            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )

            Spacer(Modifier.width(8.dp))

            Icon(
                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "折りたたむ" else "展開する",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        // コンテンツ（折りたたみ可能）
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(300),
                expandFrom = Alignment.Top
            ),
            exit = shrinkVertically(
                animationSpec = tween(300),
                shrinkTowards = Alignment.Top
            )
        ) {
            Column {
                Spacer(Modifier.height(12.dp))

                // 画像プリロード中はローディング表示
                if (!imagesPreloaded && imageUrls.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    // 横スクロールカード
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(dateGroup.suggestions) { suggestion ->
                            HistoryCoordinateCard(
                                suggestion = suggestion,
                                onClick = {
                                    navigateToGeneratedResult(
                                        suggestion = suggestion,
                                        navController = navController,
                                        suggestionViewModel = suggestionViewModel,
                                        coordinateDataMap = coordinateDataMap
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}