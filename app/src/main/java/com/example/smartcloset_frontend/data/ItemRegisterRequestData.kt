package com.example.smartcloset_frontend.data
import kotlinx.serialization.Serializable

@Serializable
data class ItemRegisterRequestData(
    val itemName: String,
    val brandName: String?,
    val size: String?,
    val category: CharCategory,
    val localImagePath: String?,
    val color: String,
    val pattern: String?,
    val material: String?,
    val feature: String?,
    val taste: String?,
    val season: String?,
    val userId: Int
)