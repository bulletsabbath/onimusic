package xyz.oniverse.music.commands.music

import xyz.oniverse.music.Oni
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context
import java.util.concurrent.ThreadLocalRandom

class Shuffle : Command() {

    init {
        name = "shuffle"
        aliases = listOf("sh", "mix")
        description = "Shuffles the current queue"
        usage = "${Oni.config.prefix}shuffle"
        category = CommandCategory.MUSIC
    }

    override fun execute(ctx: Context, reply: Reply) {
        if (ctx.vc == null) return ctx.send(reply["user-not-in-channel"])
        val player = Oni.player.getExisting(ctx.guild.idLong) ?: return ctx.send(reply["no-queue"])
        if (player.queue.size < 1) return ctx.send(reply["no-queue"])

        for (i in 0 until player.queue.size) {
            val j = ThreadLocalRandom.current().nextInt(0, player.queue.size)
            val temp = player.queue[j]
            player.queue[j] = player.queue[i]
            player.queue[i] = temp
        }

        ctx.send("Shuffled the queue!")
    }
}