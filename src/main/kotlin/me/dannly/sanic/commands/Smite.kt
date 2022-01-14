package me.dannly.sanic.commands

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import me.dannly.sanic.commands.utils.Command
import me.dannly.sanic.commands.utils.CommandInstance
import me.dannly.sanic.smite.DamageType
import me.dannly.sanic.smite.God
import me.dannly.sanic.smite.GodClass
import me.dannly.sanic.smite.Match
import net.dv8tion.jda.api.EmbedBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.regex.Pattern

class Smite : Command("smite", "Comandos relacionados a SMITE.") {

    private val godList = mutableListOf<God>()

    init {
        usages.addAll(listOf("random god|build", "match <player>", "rank <player>"))
        aliases.add("s")
        setupGodList()
    }

    private fun readURL(url: String): List<String> {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty(
            "user-agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36 OPR/79.0.4143.73"
        )
        val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8))
        return bufferedReader.readLines()
    }

    private fun getGodImage(god: God): String? {
        val godPage = readURL(god.wikiPage)
        val matcher =
            Pattern.compile("<a href=\"(https://static.wikia.nocookie.net/smite_gamepedia/images/.+?)\" class=\"image\">")
                .matcher(godPage.joinToString())
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    private fun setupGodList() {
        if (godList.isEmpty()) {
            val matcher =
                Pattern.compile("<td align=\"left\"><a href=\"(/wiki/.+?)\" title=\"(.+?)\">.+?\"/wiki/.+?\".+?\"/wiki/.+?\".+?\"/wiki/.+?\".+?\"/wiki/.+?\".+?\"/wiki/.+?\".+?\"/wiki/(.+?)\".+?\"/wiki/.+?\".+?\"/wiki/(.+?)\"")
                    .matcher(readURL("https://smite.fandom.com/wiki/List_of_gods").joinToString())
            while (matcher.find()) {
                godList.add(
                    God(
                        "https://smite.fandom.com/${matcher.group(1)}",
                        matcher.group(2).replace("&#39", "'"),
                        GodClass.valueOf(matcher.group(4)),
                        DamageType.valueOf(matcher.group(3))
                    )
                )
            }
        }
    }

    private fun sendEmbedGodMessage(commandInstance: CommandInstance, god: God) {
        commandInstance.replyWith("Deus sorteado: **${god.name}**", getGodImage(god))
    }

    private fun randomGodOfClass(commandInstance: CommandInstance) {
        commandInstance.replyWith("sorteando deus...")
        val godClass = GodClass.values().find { it.name.equals(commandInstance.arguments[1], true) }
        sendEmbedGodMessage(commandInstance, godList.filter { it.godClass == godClass }.random())
    }

    private fun argumentRandom(commandInstance: CommandInstance) {
        when (commandInstance.arguments[1].lowercase()) {
            "god" -> {
                commandInstance.replyWith("sorteando deus...")
                sendEmbedGodMessage(commandInstance, godList.random())
            }
            in GodClass.values().map { it.name.lowercase() } -> randomGodOfClass(commandInstance)
            else -> commandInstance.explainUsage()
        }
    }

    private fun readPlayerData(url: String): String {
        val call = OkHttpClient.Builder().build().newCall(Request.Builder().url(url).build())
        val response = call.execute()
        val element = response.body()?.string().orEmpty()
        response.close()
        return element
    }

    private fun getEmbedMessageForMatch(match: Match): EmbedBuilder {
        val builder = EmbedBuilder()
        builder.setTimestamp(LocalDateTime.now())
        builder.setTitle("${match.first_team.rating_average} x ${match.second_team.rating_average}")
        match.first_team.players.forEachIndexed { index, player1 ->
            val player2 = match.second_team.players.getOrNull(index)
            builder.appendDescription("${player1.name} (**${player1.rating}**) x ${player2?.name?.plus(" (**${player2.rating}**)") ?: "Hidden profile nerd"}\n")
        }
        return builder
    }

    private fun getMatchStatus(commandInstance: CommandInstance) {
        val liveMatchReadURL =
            readPlayerData("https://api.gowtherbot.com/Widgets/SmiteWidget/CurrentMatch/${commandInstance.arguments[1]}/PC")
        try {
            val match = Gson().fromJson(liveMatchReadURL, Match::class.java)
            commandInstance.replyWith(
                getEmbedMessageForMatch(match)
                    .build()
            )
        } catch (ignored: JsonSyntaxException) {
            ignored.printStackTrace()
            commandInstance.replyWith(liveMatchReadURL)
        }
    }

    private fun getPlayerRank(commandInstance: CommandInstance) {
        val liveMatchReadURL =
            readPlayerData("https://nonsocial.herokuapp.com/api/smite/rank?player=${commandInstance.arguments[1]}&language=PT")
        commandInstance.replyWith(liveMatchReadURL)
    }

    override fun run(commandInstance: CommandInstance) {
        if (!commandInstance.checkIfEnoughArguments(2)) return
        when (commandInstance.arguments[0].lowercase()) {
            "random" -> argumentRandom(commandInstance)
            "match" -> getMatchStatus(commandInstance)
            "rank" -> getPlayerRank(commandInstance)
        }
    }
}