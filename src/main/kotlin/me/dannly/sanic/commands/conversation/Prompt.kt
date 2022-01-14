package me.dannly.sanic.commands.conversation

import net.dv8tion.jda.api.entities.MessageChannel

abstract class Prompt {
    lateinit var message: String
    var messageID: Long = 0
    lateinit var channel: MessageChannel
    var escapeLine: String? = null
    var isIgnoreCase = false

    fun withLocalEcho(localEcho: Boolean): Prompt {
        isLocalEcho = localEcho
        return this
    }

    var isLocalEcho = true
        private set

    fun withEscapeLine(escapeLine: String?, ignoreCase: Boolean): Prompt {
        this.escapeLine = escapeLine
        isIgnoreCase = ignoreCase
        return this
    }

    abstract fun firstPrompt(): String
    abstract fun last(): Prompt?
}