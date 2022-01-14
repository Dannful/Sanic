package me.dannly.sanic.commands

import me.dannly.sanic.commands.utils.Command
import me.dannly.sanic.commands.utils.CommandInstance
import net.dv8tion.jda.api.entities.VoiceChannel

class MoveAll : Command(
    name = "moveall",
    description = "Move todos os usuarios de um canal de voz para outro."
) {

    private lateinit var originChannel: VoiceChannel
    private lateinit var destinationChannel: VoiceChannel

    init {
        usages.add("<id do canal de origem> <id do canal de destino?")
    }

    override fun run(commandInstance: CommandInstance) {
        if(commandInstance.checkIfEnoughArguments(2)) {
            if(!checkIfValidArgumentsAndSetFields(commandInstance)) {
                commandInstance.replyWith("insira canais válidos!")
            } else {
                moveMembers(commandInstance)
            }
        }
    }

    private fun checkIfValidArgumentsAndSetFields(commandInstance: CommandInstance) : Boolean {
        originChannel =
            (commandInstance.textChannel.guild.getVoiceChannelById(commandInstance.arguments[0].toLongOrNull() ?: -1) ?: commandInstance.textChannel.guild.getVoiceChannelsByName(commandInstance.arguments[0], true)
                .getOrNull(0)) ?: return false
        destinationChannel =
            (commandInstance.textChannel.guild.getVoiceChannelById(commandInstance.arguments[1].toLongOrNull() ?: -1) ?: commandInstance.textChannel.guild.getVoiceChannelsByName(commandInstance.arguments[1], true)
                .getOrNull(0)) ?: return false
        return true
    }

    private fun moveMembers(commandInstance: CommandInstance) {
        originChannel.members.forEach { commandInstance.textChannel.guild.moveVoiceMember(it, destinationChannel).queue() }
        commandInstance.replyWith("transferência de membros concluída!")
    }
}