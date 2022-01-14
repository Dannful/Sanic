package me.dannly.sanic.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import me.dannly.sanic.main.Bot
import me.dannly.sanic.main.Main
import net.dv8tion.jda.api.entities.VoiceChannel
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(private val player: AudioPlayer, private val guildMusicManager: GuildMusicManager) : AudioEventAdapter() {
    val queue: LinkedList<AudioTrack> = LinkedList()

    private lateinit var voiceChannel: VoiceChannel

    private lateinit var currentTrack: AudioTrack

    fun setVoiceChannel(voiceChannel: VoiceChannel) {
        this.voiceChannel = voiceChannel
        val audioManager = voiceChannel.guild.audioManager
        audioManager.sendingHandler = guildMusicManager.sendHandler
    }

    fun clear() {
        queue.clear()
    }

    fun queue(track: AudioTrack, position: Int? = null) {
        if (!player.startTrack(track, true)) {
            val q = LinkedBlockingQueue(queue.toMutableList().also { it.add(position ?: queue.size, track)})
            queue.clear()
            queue.addAll(q)
        }
    }

    fun nextTrack() {
        val poll = queue.poll()
        if(poll == null) {
            guildMusicManager.music.stop()
            return
        }
        player.startTrack(poll, false)
    }

    private fun repeatTrack() : Boolean {
        if (guildMusicManager.repeat) {
            guildMusicManager.music.load(currentTrack.info.uri) {}
            return true
        }
        return false
    }

    private fun closeConnection() {
        Bot.closeAudioConnection(guildMusicManager.music.guildID)
        Main.bot.presence.activity = Main.defaultActivity
    }

    private fun mayStartNext() {
        if(repeatTrack()) return
        if (!queue.isEmpty()) {
            nextTrack()
        } else {
            closeConnection()
        }
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        currentTrack = track
        if (endReason.mayStartNext) {
            mayStartNext()
        } else if (endReason == AudioTrackEndReason.STOPPED || endReason == AudioTrackEndReason.CLEANUP || endReason == AudioTrackEndReason.FINISHED) {
            Main.bot.presence.activity = Main.defaultActivity
        }
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        if (::voiceChannel.isInitialized && !voiceChannel.guild.audioManager.isConnected) voiceChannel.guild.audioManager.openAudioConnection(
            voiceChannel
        )
        if (track.info.isStream) {
            Main.bot.presence.activity = me.dannly.sanic.Activity.streaming(track.info.title, track.info.uri)
        } else {
            Main.bot.presence.activity = me.dannly.sanic.Activity.listening(track.info.author + " - " + track.info.title)
        }
    }
}