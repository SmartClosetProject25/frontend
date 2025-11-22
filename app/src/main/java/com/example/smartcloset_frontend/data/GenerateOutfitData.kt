package com.example.smartcloset_frontend.data

data class GenerateOutfitData(
    val userId: Int,
    val selfieId: Int,
    val topsId: Int,
    val bottomsId: Int,
    val othersId: Int? = null,
    val others2Id: Int? = null
)

data class GenerateOutfitWithWeather(
    val userId: Int,
    val plan: String,
    val weather: String,
)