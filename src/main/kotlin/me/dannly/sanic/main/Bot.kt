package me.dannly.sanic.main

object Bot {
    fun closeAudioConnection(guildID: Long) {
        for (g in Main.bot.guilds) {
            if (g.idLong == guildID) {
                val manager = g.audioManager
                if (manager.isConnected) manager.closeAudioConnection()
                break
            }
        }
    }
}