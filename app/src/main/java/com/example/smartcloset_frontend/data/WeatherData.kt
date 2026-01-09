package com.example.smartcloset_frontend.data
import kotlinx.serialization.Serializable

data class WeatherData(
    val location: String,
    val tempC: Double,
    val precipitationPercent: Int,
    val humidityPercent: Int,
    val today3h: List<Today3h>
)

data class Today3h(
    val timeLabel: String,
    val tempC: Double,
    val precipitationPercent: Int,
    val weatherType: String
)

@Serializable
data class WeatherDto(
    val location: String,
    val tempC: Double,
    val precipitationPercent: Int,
    val humidityPercent: Int,
    val today3h: List<Today3hDto>
)

@Serializable
data class Today3hDto(
    val timeLabel: String,
    val tempC: Double,
    val precipitationPercent: Int,
    val weatherType: String
)

fun WeatherDto.toWeatherData(): WeatherData =
    WeatherData(
        location = location,
        tempC = tempC,
        precipitationPercent = precipitationPercent,
        humidityPercent = humidityPercent,
        today3h = today3h.map {
            Today3h(
                timeLabel = it.timeLabel,
                tempC = it.tempC,
                precipitationPercent = it.precipitationPercent,
                weatherType = it.weatherType
            )
        }
    )