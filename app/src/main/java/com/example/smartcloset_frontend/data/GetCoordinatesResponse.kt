package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

@Serializable
data class GetCoordinatesResponse(
    val status: String,
    val coordinates: List<CoordinateData>,
    val count: Int
)

@Serializable
data class CoordinateData(
    val coordinate_id: Int,
    val top_id: Int,
    val bottom_id: Int,
    val outer_id: Int? = null,
    val scene: String,
    val features: Map<String, String>,
    val created_at: String,
    val top: CoordinateItem,
    val bottom: CoordinateItem,
    val outer: CoordinateItem? = null
)

@Serializable
data class CoordinateItem(
    val id: Int,
    val name: String,
    val image_path: String
)

