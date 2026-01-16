package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

@Serializable
data class TodayPlanData(
    val id: Int?,
    val plan: String,
    val date: String,
    val location: String,
    val weather: String,
    val precipitation: String,
    val humidity: String,
    val gender: String? = null
)

@Serializable
data class ProposalResponse(
    val proposals: List<Proposal>
)

@Serializable
data class Proposal(
    val pattern: Int,
    val items: Items,
    val item_ids: List<Int>,
    val reason: String,
    val coordinate_id: Int? = null
)

@Serializable
data class Items(
    val tops: Item?,
    val bottoms: Item?,
    val outer: Item?
)

@Serializable
data class Item(
    val id: Int,
    val item_name: String,
    val image_path: String,
    val taste: List<String>
)
