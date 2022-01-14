package me.dannly.sanic.commands

import com.google.api.services.youtube.model.SearchResult
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.dannly.sanic.commands.utils.Command
import me.dannly.sanic.commands.utils.CommandInstance
import me.dannly.sanic.music.IndexedTrack
import me.dannly.sanic.music.MusicManager
import me.dannly.sanic.utils.Search
import me.dannly.sanic.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor

class Music : Command(
    name = "music",
    description = "Toca música."
) {

    init {
        usages.addAll(
            listOf(
                "play | queue <nome/link>",
                "list",
                "next <nome/link>",
                "stop",
                "remove <id>",
                "pause",
                "volume <valor>",
                "skip",
                "info",
                "clear"
            )
        )
        aliases.add("m")
    }

    private fun processQueue(commandInstance: CommandInstance, position: Int? = null) {
        val voiceState = commandInstance.textChannel.guild.getMember(commandInstance.author)?.voiceState
        commandInstance.data["songPositionAndQuery"] = IndexedTrack(position, null)
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            commandInstance.replyWith(
                "você precisa estar em um canal de voz para executar esse comando!"
            )
        } else {
            processSongQuery(commandInstance)
        }
    }

    private fun processSongQuery(commandInstance: CommandInstance) {
        val indexedTrack = commandInstance.data["songPositionAndQuery"] as IndexedTrack
        indexedTrack.trackQuery = commandInstance.arguments.copyOfRange(1, commandInstance.arguments.size).joinToString(" ").replace("\"", "")
        if (!Utils.isURLValid(indexedTrack.trackQuery.orEmpty()) && !File(indexedTrack.trackQuery.orEmpty()).exists()) {
            indexedTrack.trackQuery = searchRetrieveURLAndLog(commandInstance)
        }
        loadAndPlaySongIntoPlayer(commandInstance)
    }

    private fun loadAndPlaySongIntoPlayer(commandInstance: CommandInstance) {
        val music = commandInstance.data["manager"] as MusicManager.Music
        val indexedTrack = commandInstance.data["songPositionAndQuery"] as IndexedTrack
        music.guildMusicManager.scheduler.setVoiceChannel(commandInstance.textChannel.guild.getMember(commandInstance.author)!!.voiceState!!.channel!!)
        indexedTrack.trackQuery?.let { query ->
            music.load(query, indexedTrack.position) {
                if(it == null) {
                    commandInstance.replyWith("playlist adicionada com sucesso!")
                } else {
                    commandInstance.replyWith(
                        "música **${it.second.info.author} - ${it.second.info.title}** adicionada na posição ${it.first}."
                    )
                }
            }
        }
    }

    private fun searchRetrieveURLAndLog(commandInstance: CommandInstance) : String? {
        val indexedTrack = commandInstance.data["songPositionAndQuery"] as IndexedTrack
        indexedTrack.trackQuery?.let {
            commandInstance.replyWith("pesquisando música com o nome **$it**...")
            val search: List<SearchResult> = Search.search(it, 1)
            return if (search.isNotEmpty()) {
                val songURL = "https://www.youtube.com/watch?v=" + search[0].id.videoId
                commandInstance.replyWith("música encontrada: $songURL")
                songURL
            } else {
                commandInstance.replyWith("nenhuma música encontrada com o nome **$it**.")
                null
            }
        }
        return null
    }

    private fun adjustVolume(commandInstance: CommandInstance) {
        val toIntOrNull = commandInstance.arguments[1].toIntOrNull()
        if (toIntOrNull != null) {
            (commandInstance.data["manager"] as MusicManager.Music).setVolume(toIntOrNull)
            commandInstance.replyWith("volume definido para **${commandInstance.arguments[1]}**.")
        } else {
            commandInstance.replyWith("valor de volume inválido.")
        }
    }

    private fun removeSongFromQueue(commandInstance: CommandInstance) {
        val toIntOrNull = commandInstance.arguments[1].toIntOrNull()
        val music = commandInstance.data["manager"] as MusicManager.Music
        if (toIntOrNull == null || music.guildMusicManager.scheduler.queue.size <= toIntOrNull - 1) {
            commandInstance.replyWith("insira um ID válido!")
            return
        }
        val song = music.removeFromQueue(toIntOrNull - 1) ?: return
        commandInstance.replyWith("música **${song.info.author} - ${song.info.title}** removida!")
    }

    private fun skipCurrentSong(commandInstance: CommandInstance) {
        (commandInstance.data["manager"] as MusicManager.Music).skip()
        commandInstance.replyWith("música pulada.")
    }

    private fun stopPlayer(commandInstance: CommandInstance) {
        (commandInstance.data["manager"] as MusicManager.Music).stop()
        commandInstance.replyWith("conexão terminada.")
    }

    private fun showPlaylist(commandInstance: CommandInstance) {
        val musicQueue = (commandInstance.data["manager"] as MusicManager.Music).guildMusicManager.scheduler.queue
        commandInstance.replyWith("lista de músicas: \n${musicQueue.joinToString("\n") { "**${musicQueue.indexOf(it) + 1}**. ${it.info.author} - ${it.info.title}" }.ifEmpty { "\n- Nenhuma" }}")
    }

    private fun pausePlayer(commandInstance: CommandInstance) {
        val music = commandInstance.data["manager"] as MusicManager.Music
        music.pause()
        commandInstance.replyWith(
            "player **" + (if (music.guildMusicManager.player.isPaused) "pausado" else "despausado") + "**."
        )
    }

    private fun adjustPlayerRepeat(commandInstance: CommandInstance) {
        val music = commandInstance.data["manager"] as MusicManager.Music
        music.repeat = !music.repeat
        commandInstance.replyWith("repetir: **${(if (music.repeat) "ON" else "OFF")}**")
    }

    private fun checkAndLogIfThereIsAnySongPlaying(commandInstance: CommandInstance) : Boolean {
        if((commandInstance.data["manager"] as MusicManager.Music).guildMusicManager.player.playingTrack == null) {
            commandInstance.replyWith(
                "nenhuma música está sendo tocada no momento."
            )
            return false
        }
        return true
    }

    private fun displaySongInfo(commandInstance: CommandInstance) {
        if(!checkAndLogIfThereIsAnySongPlaying(commandInstance)) return
        val playingTrack = (commandInstance.data["manager"] as MusicManager.Music).guildMusicManager.player.playingTrack
        commandInstance.replyWith(
            EmbedBuilder().setTitle("${playingTrack.info.author} - ${playingTrack.info.title}").setTimestamp(
                Date().toInstant()
            ).setDescription(getSongInfoString(playingTrack)).build()
        )
    }

    private fun getSongInfoString(playingTrack: AudioTrack) : String {
        val position = floor(
            playingTrack.position.toDouble() / playingTrack.duration
                .toDouble() * 10
        )
        var builder = StringBuilder(timestampStringFromMillis(playingTrack.position) + " ")
        builder = appendSongProgressTextToBuilderAndReturn(builder, position)
        builder.append(" ").append(timestampStringFromMillis(playingTrack.duration))
        return builder.toString()
    }

    private fun appendSongProgressTextToBuilderAndReturn(builder: StringBuilder, position: Double) : StringBuilder {
        for (i in 0..10) {
            if (i.toDouble() == position) {
                builder.append(":white_circle:")
                continue
            }
            if (i < position) {
                builder.append("<:redline:789229605067948045>")
                continue
            }
            builder.append("<:grayline:789229951249285141>")
        }
        return builder
    }

    private fun clearMusicQueue(commandInstance: CommandInstance) {
        (commandInstance.data["manager"] as MusicManager.Music).guildMusicManager.scheduler.queue.clear()
        commandInstance.replyWith("fila esvaziada.")
    }

    override fun run(commandInstance: CommandInstance) {
        commandInstance.data["manager"] = MusicManager.getManager(commandInstance.textChannel.guild.idLong)
        when(commandInstance.arguments.size) {
            in 2..Integer.MAX_VALUE -> {
                when (commandInstance.arguments[0].toLowerCase()) {
                    "queue", "next", "play" -> processQueue(commandInstance, if (commandInstance.arguments[0].equals("next", true)) 0 else null)
                    "volume" -> adjustVolume(commandInstance)
                    "remove" -> removeSongFromQueue(commandInstance)
                    else -> commandInstance.explainUsage()
                }
            }
            1 -> {
                when (commandInstance.arguments[0].toLowerCase()) {
                    "skip" -> skipCurrentSong(commandInstance)
                    "stop" -> stopPlayer(commandInstance)
                    "list" -> showPlaylist(commandInstance)
                    "pause" -> pausePlayer(commandInstance)
                    "repeat" -> adjustPlayerRepeat(commandInstance)
                    "info" -> displaySongInfo(commandInstance)
                    "clear" -> clearMusicQueue(commandInstance)
                    else -> commandInstance.explainUsage()
                }
            }
            else -> commandInstance.explainUsage()
        }
    }

    private fun timestampStringFromMillis(millis: Long): String {
        var format = String.format(
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        )
        if (format.startsWith("00:")) format = format.substring(3)
        return format
    }
}