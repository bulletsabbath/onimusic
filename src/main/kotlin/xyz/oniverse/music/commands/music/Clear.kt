package xyz.oniverse.music.commands.music

import xyz.oniverse.music.Oni
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context

class Clear : Command() {
    init {
        name = "clear"
        aliases = listOf("clean", "cls", "clearqueue", "cq")
        description = "Clears the queue"
        category = CommandCategory.MUSIC
        usage = "${Oni.config.prefix}clear"
    }
    override fun execute(ctx: Context, reply: Reply) {
        val player = Oni.player.getExisting(ctx.guild.idLong) ?: return ctx.send(reply["bot-not-in-channel"])

        if (ctx.vc == null) return ctx.send(reply["user-not-in-channel"])
        if (ctx.vc != ctx.guild.selfMember.voiceState?.channel) return ctx.send(reply["different-channels"])
        if (player.queue.isEmpty()) return ctx.send(reply["nothing-in-queue"])

        player.queue.clear().also {
            ctx.send("Successfully emptied queue!")
        }
    }
}