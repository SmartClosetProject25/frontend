package com.example.smartcloset_frontend.data

import kotlinx.serialization.Serializable

@Serializable
data class TodayPlanData(
    val plan: String,
    val date: String,
    val location: String,
    val weather: String,
    val precipitation: String,
    val humidity: String
)

@Serializable
data class ProposalResponse(
    val proposals: List<Proposal>
)

@Serializable
data class Proposal(
    val pattern: Int,
    val items: Items,
    val item_ids: List<String>,
    val reason: String
)

@Serializable
data class Items(
    val tops: String?,
    val bottoms: String?,
    val outer: String?
)
