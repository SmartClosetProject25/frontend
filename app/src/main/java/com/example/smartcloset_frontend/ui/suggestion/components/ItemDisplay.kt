package com.example.smartcloset_frontend.ui.suggestion.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcloset_frontend.data.Item
import com.example.smartcloset_frontend.ui.common.RetryableImage

/**
 * アイテム表示コンポーネント
 */
@Composable
fun ItemDisplay(
    item: Item?,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 画像
        Box(
            modifier = Modifier
                .size(width = 85.dp, height = 110.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFFC0C0C0), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (item != null && item.image_path.isNotBlank()) {
                RetryableImage(
                    imagePath = item.image_path,
                    contentDescription = item.item_name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(label, color = Color.Gray, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // テキスト
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item?.item_name ?: label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item?.taste?.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE0F7FA), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            color = Color(0xFF2196F3),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}