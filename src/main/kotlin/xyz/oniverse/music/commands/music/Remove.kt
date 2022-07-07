package xyz.oniverse.music.commands.music

import xyz.oniverse.music.Oni
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context

class Remove : Command() {

    init {
        name = "remove"
        aliases = listOf("rm", "rmv", "delete")
        description = "Removes the desired song from the queue"
        usage = "${Oni.config.prefix}remove {song index}"
        category = CommandCategory.MUSIC
    }

    override fun execute(ctx: Context, reply: Reply) {
        val player = Oni.player.getExisting(ctx.guild.idLong) ?: return ctx.send(reply["no-queue"])
        if (player.queue.size < 1) return ctx.send(reply["no-queue"])

        println(ctx.args[0])
        try {
            val song = player.queue.removeAt(ctx.args[0].toInt().minus(1))
            ctx.send("Successfully removed song!")
        } catch (e: Exception) {
            println(e.message)
            ctx.send("Couldn't remove the song, sorry! Please try again later.")
        }

    }
}