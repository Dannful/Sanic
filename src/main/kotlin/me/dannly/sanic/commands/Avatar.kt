package me.dannly.sanic.commands

import me.dannly.sanic.commands.utils.Command
import me.dannly.sanic.commands.utils.CommandInstance
import net.dv8tion.jda.api.entities.User

class Avatar : Command(name = "avatar", description = "Visualiza o avatar de um usuário.") {

    private lateinit var target: User
    private lateinit var avatarUrl: String

    init {
        usages.add("<usuario> (dimensoes)")
    }

    override fun run(commandInstance: CommandInstance) {
        if(commandInstance.checkIfEnoughArguments(1) && checkIfUserIsValid(commandInstance) && checkIfUserHasAvatar(commandInstance))
            retrieveAndPrintAvatar(commandInstance)
    }

    private fun checkIfUserIsValid(commandInstance: CommandInstance) : Boolean {
        val target = commandInstance.message.mentionedUsers[0]
        if(target == null) {
            commandInstance.replyWith("usuário inválido!")
            return false
        }
        this.target = target
        return true
    }

    private fun checkIfUserHasAvatar(commandInstance: CommandInstance) : Boolean {
        avatarUrl = target.avatarUrl.orEmpty()
        if(avatarUrl.isEmpty()) {
            commandInstance.replyWith("o usuário não possui um ícone!")
            return false
        }
        return true
    }

    private fun retrieveAndPrintAvatar(commandInstance: CommandInstance) {
        if(commandInstance.arguments.size >= 2)
            if(!appendSpecificSizeOrSendErrorMessage(commandInstance))
                return
        commandInstance.replyWith(avatarUrl)
    }

    private fun appendSpecificSizeOrSendErrorMessage(commandInstance: CommandInstance) : Boolean {
        val size = commandInstance.arguments[1].toIntOrNull()
        if (size == null || !arrayOf(64, 128, 256, 512).contains(size)) {
            commandInstance.replyWith("valor inválido de dimensão! Possíveis valores: 64, 128, 256 e 512.")
            return false
        }
        avatarUrl += "?size=$size"
        return true
    }
}