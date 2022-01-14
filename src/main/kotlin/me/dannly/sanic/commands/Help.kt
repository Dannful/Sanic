package me.dannly.sanic.commands

import me.dannly.sanic.commands.utils.Command
import me.dannly.sanic.commands.utils.CommandInstance

class Help : Command(name = "help", description = "Mostra ajuda quanto a um comando.") {

    init {
        usages.add("<comando>")
    }

    override fun run(commandInstance: CommandInstance) {
        if (commandInstance.checkIfEnoughArguments(1))
            sendHelpOrErrorMessage(commandInstance)
    }

    private fun sendHelpOrErrorMessage(commandInstance: CommandInstance) {
        val find = commands.find { it.name.equals(commandInstance.arguments[0], true) }
        if (find == null) {
            commandInstance.replyWith("comando inválido!")
        } else {
            commandInstance.replyWith(helpMessage())
        }
    }
}