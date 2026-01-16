package com.example.smartcloset_frontend.ui.suggestion.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcloset_frontend.data.WeatherData

/**
 * 天気情報を表示するカードコンポーネント
 */
@Composable
fun WeatherCard(
    weatherData: WeatherData?,
    modifier: Modifier = Modifier
) {
    val locationText = weatherData?.location ?: "取得中..."
    val tempText = weatherData?.tempC?.let { "${it}℃" } ?: "--℃"
    val popText = weatherData?.precipitationPercent?.let { "${it}%" } ?: "--%"
    val humText = weatherData?.humidityPercent?.let { "${it}%" } ?: "--%"
    val emoji = when (weatherData?.today3h?.firstOrNull()?.weatherType) {
        "clear" -> "☀️"
        "rain" -> "🌧️"
        "snow" -> "❄️"
        "cloud" -> "☁️"
        else -> "☁️"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(locationText, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tempText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column {
                        Text("降水確率", fontSize = 12.sp, color = Color.Gray)
                        Text(popText, fontSize = 14.sp)
                    }
                    Column {
                        Text("湿度", fontSize = 12.sp, color = Color.Gray)
                        Text(humText, fontSize = 14.sp)
                    }
                }
            }
            // 3時間ごとの天気予報
            val list = weatherData?.today3h.orEmpty()
            if (list.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(list) { h ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(h.timeLabel, fontSize = 12.sp, color = Color.Gray)
                            Text("${h.tempC}℃", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${h.precipitationPercent}%", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}