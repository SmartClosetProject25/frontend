package com.example.smartcloset_frontend.data
import kotlinx.serialization.Serializable
@Serializable
data class AddItemData (
    val color: Int,
    val pattern: Int,
    val size: Int,
    val brand: String,
    val category: Int,
    val userId : Int,
    val imageUrl: String,
    val material: String,
    val feature: String,
    val taste: String,
    val season: String,
    val itemName: String
)