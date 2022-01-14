package me.dannly.sanic.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.util.function.Consumer

class QueueResultHandler(private val guildMusicManager: GuildMusicManager) : AudioLoadResultHandler {

    var position: Int? = null
    lateinit var onQueueSuccess: Consumer<Pair<Int, AudioTrack>?>

    override fun trackLoaded(receivedTrack: AudioTrack) {
        val givenPositionOrFirst = position ?: guildMusicManager.scheduler.queue.size + 1
        guildMusicManager.scheduler.queue(receivedTrack, position)
        onQueueSuccess.accept(givenPositionOrFirst to receivedTrack)
    }

    override fun playlistLoaded(receivedPlaylist: AudioPlaylist) {
        receivedPlaylist.tracks.forEach {
            guildMusicManager.scheduler.queue(it)
        }
        onQueueSuccess.accept(null)
    }

    override fun noMatches() {
    }

    override fun loadFailed(errorException: FriendlyException?) {
    }
}