package me.dannly.sanic.commands.utils

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import java.util.*

abstract class Command(
    val name: String,
    val description: String
) {
    val permissions = mutableListOf<Permission>()
    val aliases = mutableListOf<String>()
    val usages = mutableListOf<String>()

    fun helpMessage(): MessageEmbed {
        return EmbedBuilder().setTitle("Ajuda: ${aliases.toMutableList().also { it.add(name) }.sorted().joinToString("/")}").setTimestamp(
            Date().toInstant())
            .addField("Uso(s)", usages.joinToString("\n"), true).addField("Descrição", description, true).build()
    }

    abstract fun run(commandInstance: CommandInstance)

    companion object {
        val commands = mutableListOf<Command>()
    }
}