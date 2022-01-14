package me.dannly.sanic.smite

data class Team(
    val players: List<Player>,
    val rank_average: String,
    val rating_average: Int,
    val win_rate_average_percentage: Int
)