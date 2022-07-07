package xyz.oniverse.music.commands.music

import xyz.oniverse.music.Oni
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context

class MoveSong : Command() {

    init {
        name = "move"
        aliases = listOf("moveSong", "mv")
        description = "Moves song from its place in queue to the desired place"
        usage = "${Oni.config.prefix}move {song index} {desired index}"
        category = CommandCategory.MUSIC
    }

    override fun execute(ctx: Context, reply: Reply) {
        if (ctx.vc == null) return ctx.send(reply["user-not-in-channel"])
        val player = Oni.player.getExisting(ctx.guild.idLong) ?: return ctx.send(reply["no-queue"])
        if (player.queue.size < 1) return ctx.send(reply["no-queue"])

        val removed = player.queue.removeAt(ctx.args[0].toInt().minus(1))
        player.queue.add(ctx.args[1].toInt().minus(1), removed)
        ctx.send("Successfully moved song.")
    }
}