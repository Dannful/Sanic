package me.dannly.sanic.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager

class GuildMusicManager(val manager: AudioPlayerManager, val music: MusicManager.Music) {
    val player: AudioPlayer = manager.createPlayer()
    val scheduler: TrackScheduler = TrackScheduler(player, this)
    val sendHandler: AudioPlayerSendHandler
        get() = AudioPlayerSendHandler(player)

    var repeat = false

    init {
        player.addListener(scheduler)
    }
}