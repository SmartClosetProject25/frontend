package com.example.smartcloset_frontend.ui.suggestion_history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcloset_frontend.data.CoordinateItem
import com.example.smartcloset_frontend.ui.common.ImageUrlHelper
import com.example.smartcloset_frontend.ui.common.RetryableImage

/**
 * 履歴アイテムグリッドセルコンポーネント
 */
@Composable
fun HistoryItemGridCell(
    item: CoordinateItem,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 画像
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f) // 縦長の比率
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE0E0E0))
                .border(1.dp, Color(0xFFC0C0C0), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            val imageUrl = ImageUrlHelper.buildImageUrl(item.image_path)
            if (imageUrl != null) {
                RetryableImage(
                    imagePath = item.image_path,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(label, color = Color.Gray, fontSize = 9.sp)
            }
        }

        // ラベルとアイテム名
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}