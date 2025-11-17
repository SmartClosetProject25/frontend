package com.example.smartcloset_frontend.data
import kotlinx.serialization.Serializable
@Serializable
data class ItemData (
    val id: Int,
    val color: String,
    val category: String,
    val imageUrl: String
)
