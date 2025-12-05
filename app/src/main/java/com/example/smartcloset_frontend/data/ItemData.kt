package com.example.smartcloset_frontend.data
import kotlinx.serialization.Serializable
@Serializable
data class ItemData (
    val id: Int,
    val itemName: String,
    val category: Int,
    val imageUrl: String?=null
)
@Serializable
data class GetItemsResponse(
    val status: String,
    val items: List<ItemData>
)

@Serializable
data class ItemDetailData (
    val id: Int,
    val itemName: String,
    val brandName: String?,
    val size: Int,
    val category: Int,
    val color: Int,
    val pattern: Int,
    val material: Int,
    val feature: Int,
    val taste: Int,
    val season: Int,
    val imageUrl: String
)

@Serializable
data class JudgeRequestData(
    val planItemId: Int,
    val vote: String  // "GOOD" or "BAD"
)
