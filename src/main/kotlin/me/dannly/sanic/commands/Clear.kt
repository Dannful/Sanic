package me.dannly.sanic.commands

import me.dannly.sanic.commands.utils.Command
import me.dannly.sanic.commands.utils.CommandInstance
import net.dv8tion.jda.api.Permission

class Clear : Command("clear", description = "Limpa um chat do servidor.") {

    init {
        permissions.add(Permission.MANAGE_CHANNEL)
        usages.add("<número de mensagens>")
    }

    override fun run(commandInstance: CommandInstance) {
        if (commandInstance.arguments.isEmpty()) {
            commandInstance.textChannel.delete().queue { commandInstance.textChannel.createCopy().queue() }
        } else {
            deleteAmountOfMessages(commandInstance)
        }
    }

    private fun deleteAmountOfMessages(commandInstance: CommandInstance) {
        commandInstance.data["amountOfMessages"] = commandInstance.arguments[0].toIntOrNull()
        if (checkIfValidDeleteAmount(commandInstance))
            commandInstance.message.delete().queue {
                commandInstance.textChannel.history.retrievePast(commandInstance.data["amountOfMessages"] as Int).queue { commandInstance.textChannel.deleteMessages(it).queue() }
            }
    }

    private fun checkIfValidDeleteAmount(commandInstance: CommandInstance): Boolean {
        val userProvided = commandInstance.data["amountOfMessages"]
        if (userProvided == null || userProvided !in 2..100) {
            commandInstance.replyWith("insira um número de 2 a 100!")
            return false
        }
        return true
    }
}