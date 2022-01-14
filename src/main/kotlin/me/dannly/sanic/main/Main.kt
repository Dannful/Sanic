package me.dannly.sanic.main

import me.dannly.sanic.AuthToken
import me.dannly.sanic.commands.utils.Command
import me.dannly.sanic.commands.utils.CommandInterpreter
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import org.reflections8.Reflections

class Main {

    private fun startJDA() {
        bot = JDABuilder.createDefault(AuthToken.TOKEN)
            .addEventListeners(CommandInterpreter()).build()
        bot.presence.activity = defaultActivity
    }

    private fun instantiateCommands() {
        val reflections = Reflections("me.dannly.sanic.commands")
        val subTypesOf: MutableSet<Class<out Command>> = reflections.getSubTypesOf(
            Command::class.java
        )
        subTypesOf.remove(Command::class.java)
        subTypesOf.forEach {
            Command.commands.add(it.newInstance() as Command)
        }
    }

    init {
        instantiateCommands()
        startJDA()
    }

    companion object {
        val defaultActivity : Activity = me.dannly.sanic.Activity.getActivity()
        lateinit var bot: JDA
        const val prefix = "!"

        @JvmStatic
        fun main(args: Array<String>) {
            Main()
        }
    }
}