package me.dannly.sanic.smite

data class Match(
    val first_team: Team,
    val game_mode: String,
    val is_ranked_match: Boolean,
    val match_id: Int,
    val second_team: Team
)