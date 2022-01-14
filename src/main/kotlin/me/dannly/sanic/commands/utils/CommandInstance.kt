package me.dannly.sanic.commands.utils

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import java.util.*

class CommandInstance(
    val textChannel: TextChannel,
    val author: User,
    val message: Message,
    val arguments: Array<String>
) {

    lateinit var command: Command

    private lateinit var lastEmbedBuilder: EmbedBuilder
    private lateinit var lastMessage: Message
    private lateinit var answer: String
    private lateinit var imageURL: String

    val data = mutableMapOf<String, Any?>()

    private val defaultEmbedBuilder: EmbedBuilder
        get() {
            return EmbedBuilder().setTitle(
                command.aliases.toMutableList().also { it.add(command.name) }.sorted()
                    .joinToString("/"), message.jumpUrl
            ).setTimestamp(Date().toInstant())
        }

    private fun setDescriptionAndUpdateMessage() {
        lastEmbedBuilder = lastEmbedBuilder.setDescription("\n${answer.capitalize()}")
        if(::imageURL.isInitialized)
            lastEmbedBuilder.setImage(imageURL)
        lastMessage.editMessage(lastEmbedBuilder.build()).queue()
    }

    private fun setupFirstEmbed() {
        lastEmbedBuilder = defaultEmbedBuilder.setDescription(answer)
        if(::imageURL.isInitialized)
            lastEmbedBuilder.setImage(imageURL)
        lastMessage = textChannel.sendMessage(lastEmbedBuilder.build()).complete()
    }

    private fun setupFirstEmbedOrUpdateLastMessage() {
        if (!::lastEmbedBuilder.isInitialized) {
            setupFirstEmbed()
        } else {
            setDescriptionAndUpdateMessage()
        }
    }

    fun replyWith(answer: String, imageURL: String? = null) {
        val currentTextPlusAnswer =
            if (::lastEmbedBuilder.isInitialized) "${lastEmbedBuilder.descriptionBuilder.toString()}\n${answer.capitalize()}" else "${author.asMention} $answer"
        this.answer = if (currentTextPlusAnswer.length > 2048) "${
            currentTextPlusAnswer.substring(
                0,
                2045
            )
        }..." else currentTextPlusAnswer
        if(imageURL != null)
            this.imageURL = imageURL
        setupFirstEmbedOrUpdateLastMessage()
    }

    fun explainUsage() {
        textChannel.sendMessage(command.helpMessage()).queue()
    }

    fun checkIfEnoughArguments(minArgumentsAmount: Int): Boolean {
        if (arguments.size < minArgumentsAmount) {
            explainUsage()
            return false
        }
        return true
    }

    fun replyWith(message: MessageEmbed) {
        textChannel.sendMessage(message).queue()
    }
}