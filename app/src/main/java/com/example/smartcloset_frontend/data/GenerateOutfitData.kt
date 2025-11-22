package com.example.smartcloset_frontend.data
import android.graphics.Bitmap

data class GenerateOutfitData(
    val userId: Int,
    val selfieId: Int,
    val topsId: Int,
    val bottomsId: Int,
    val othersId: Int? = null,
    val others2Id: Int? = null
)