package xyz.oniverse.music.commands.misc

import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context

class Ping : Command() {

    init {
        name = "ping"
        aliases = listOf("pong")
        category = CommandCategory.MISC
    }

    override fun execute(ctx: Context, reply: Reply) {
        val latency = ctx.jda.restPing.complete()
        ctx.channel.sendMessage("Pinging...").queue {
            it.editMessage(":ping_pong: | Pong! Latency is `$latency` ms.").queue()
        }
    }
}