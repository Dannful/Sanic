package me.dannly.sanic.commands.conversation

import me.dannly.sanic.main.Main
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class Conversation(private val user: User, private val textChannel: TextChannel) : ListenerAdapter() {
    private lateinit var prompt: Prompt
    private var conversationAbandonedListener: ConversationAbandonedListener? = null
    private lateinit var guildMessageReceivedEvent: GuildMessageReceivedEvent

    fun withPrompt(prompt: Prompt): Conversation {
        val message = prompt.firstPrompt()
        if (message.isNotEmpty()) textChannel.sendMessage(message).queue()
        this.prompt = prompt
        return this
    }

    private fun endConversation() {
        Main.bot.removeEventListener(this)
        if (conversationAbandonedListener != null) conversationAbandonedListener!!.abandon()
    }

    fun addConversationAbandonedListener(conversationAbandonedListener: ConversationAbandonedListener?): Conversation {
        this.conversationAbandonedListener = conversationAbandonedListener
        return this
    }

    private fun nextPromptOrFinalize() {
        val last = prompt.last()
        if (last == null) {
            endConversation()
        } else {
            withPrompt(last)
            if (!prompt.isLocalEcho) guildMessageReceivedEvent.message.delete().queue()
        }
    }

    private fun endConversationIfEscapeLine(): Boolean {
        prompt.escapeLine?.let {
            if (guildMessageReceivedEvent.message.contentStripped.equals(it, prompt.isIgnoreCase)) {
                endConversation()
                return true
            }
        }
        return false
    }

    private fun setPromptVariables() {
        prompt.channel = guildMessageReceivedEvent.channel
        prompt.messageID = guildMessageReceivedEvent.message.idLong
        prompt.message = guildMessageReceivedEvent.message.contentStripped
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        guildMessageReceivedEvent = event
        if (event.author == user) {
            if (::prompt.isInitialized) {
                if (endConversationIfEscapeLine()) return
                setPromptVariables()
                nextPromptOrFinalize()
            }
        }
    }

    init {
        Main.bot.addEventListener(this)
    }
}