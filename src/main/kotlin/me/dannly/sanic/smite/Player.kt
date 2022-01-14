package me.dannly.sanic.smite

data class Player(
    val god_losses: Int,
    val god_name: String,
    val god_portrait: String,
    val god_wins: Int,
    val god_worshippers: Int,
    val name: String,
    val rank_tier: String,
    val rating: Int
)