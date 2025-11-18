package com.example.smartcloset_frontend.data
import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val country: String,
    val city: String,
    //    降水確率＞cor
    val cor: Int,
    val temperature: Double,
    val humidity: Int
)