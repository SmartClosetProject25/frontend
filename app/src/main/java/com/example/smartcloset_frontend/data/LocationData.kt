package com.example.smartcloset_frontend.data
import kotlinx.serialization.Serializable

@Serializable
data class LocationData(
    val lat: Double,
    val lon: Double
)