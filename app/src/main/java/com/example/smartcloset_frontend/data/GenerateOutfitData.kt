package com.example.smartcloset_frontend.data
import kotlinx.serialization.Serializable

@Serializable
data class GenerateOutfitData(
    val plan: String,
    val temperature: Int,
//    降水確率＞cor
    val cor: Int,
)