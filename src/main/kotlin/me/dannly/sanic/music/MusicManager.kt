package me.dannly.sanic.music

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.dannly.sanic.main.Bot
import java.util.function.Consumer

object MusicManager {

    private val managers = mutableMapOf<Long, GuildMusicManager>()

    fun getManager(guildID: Long): Music {
        return Music(guildID)
    }

    class Music(val guildID: Long) {

        val guildMusicManager = managers.getOrDefault(guildID, GuildMusicManager(DefaultAudioPlayerManager(), this))

        init {
            if (!managers.containsKey(guildID) || managers[guildID] != guildMusicManager)
                managers[guildID] = guildMusicManager
            AudioSourceManagers.registerRemoteSources(guildMusicManager.manager)
            AudioSourceManagers.registerLocalSource(guildMusicManager.manager)
        }

        fun load(query: String, position: Int? = null, onSuccess: Consumer<Pair<Int, AudioTrack>?>) {
            val manager = guildMusicManager.manager
            manager.loadItem(query, QueueResultHandler(guildMusicManager).also {
                it.position = position
                it.onQueueSuccess = onSuccess
            })
        }

        fun pause(): Boolean {
            guildMusicManager.player.isPaused =
                !guildMusicManager.player.isPaused
            return guildMusicManager.player.isPaused
        }

        fun setVolume(volume: Int) {
            if (volume in 0..100) {
                guildMusicManager.player.volume = volume
            }
        }

        fun skip() {
            guildMusicManager.scheduler.nextTrack()
        }

        fun removeFromQueue(id: Int) : AudioTrack? {
            return try { guildMusicManager.scheduler.queue.removeAt(id) } catch(exception: IndexOutOfBoundsException) { null }
        }

        fun stop() {
            guildMusicManager.player.stopTrack()
            guildMusicManager.scheduler.clear()
            managers.remove(guildID)
            Bot.closeAudioConnection(guildID)
        }

        var repeat : Boolean
            get() {
                return guildMusicManager.repeat
            }
            set(value) {
                guildMusicManager.repeat = value
            }
    }
}