package me.dannly.sanic.commands.utils

import me.dannly.sanic.main.Main
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CommandInterpreter : ListenerAdapter() {

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.message.contentRaw.isNotBlank() && event.message.contentRaw.startsWith(Main.prefix)) {
            val msg = event.message.contentRaw.substring(1).split(" ").toTypedArray()
            val args = if (msg.size >= 2) msg.copyOfRange(1, msg.size) else arrayOf()
            val commandInstance = CommandInstance(event.channel, event.author, event.message, args)
            commandInstance.data["commandName"] = msg[0]
            run(commandInstance)
        }
    }

    private fun runCommand(commandInstance: CommandInstance, executedCommand: Command) {
        if (commandInstance.arguments.isNotEmpty() && commandInstance.arguments[0].equals("help", true)) {
            commandInstance.explainUsage()
            return
        }
        commandInstance.command = executedCommand
        executedCommand.run(commandInstance)
    }

    private fun findCommand(commandInstance: CommandInstance): Command? {
        return Command.commands.find {
            (it.name.equals(commandInstance.data["commandName"] as String, true)
                    || it.aliases.any { c ->
                c.equals(
                    commandInstance.data["commandName"] as String,
                    true
                )
            }) && (it.permissions.isEmpty() || commandInstance.textChannel.guild.getMember(commandInstance.author)
                ?.hasPermission(it.permissions) == true)
        }
    }

    private fun run(commandInstance: CommandInstance) {
        val executedCommand = findCommand(commandInstance)
        if (executedCommand != null) {
            runCommand(commandInstance, executedCommand)
        }
    }

    override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
        if (event.message.contentRaw.isNotBlank() && event.message.contentRaw.startsWith(Main.prefix)) {
            val msg = event.message.contentRaw.substring(1).split(" ").toTypedArray()
            val args = if (msg.size >= 2) msg.copyOfRange(1, msg.size) else arrayOf()
            val commandInstance = CommandInstance(event.channel, event.author, event.message, args)
            commandInstance.data["commandName"] = msg[0]
            run(commandInstance)
        }
    }
}