package me.dannly.sanic.commands

import me.dannly.sanic.commands.utils.Command
import me.dannly.sanic.commands.utils.CommandInstance
import me.dannly.sanic.main.Main
import net.dv8tion.jda.api.Permission

class Shutdown : Command(name = "shutdown", description = "Shuts down the bot.") {

    init {
        permissions.add(Permission.ADMINISTRATOR)
    }

    override fun run(commandInstance: CommandInstance) {
        Main.bot.shutdownNow()
    }
}