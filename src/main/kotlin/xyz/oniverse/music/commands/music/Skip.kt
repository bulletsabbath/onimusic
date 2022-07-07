package xyz.oniverse.music.commands.music

import xyz.oniverse.music.Oni
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context

class Skip : Command() {

    init {
        name = "skip"
        aliases = listOf("s", "sk", "skipto")
        description = "skips to the desired song"
        category = CommandCategory.MUSIC
        usage = "${Oni.config.prefix}skip [index]"
    }

    override fun execute(ctx: Context, reply: Reply) {
        val player = Oni.player.getExisting(ctx.guild.idLong)

        if (!check(ctx, reply, player)) return

        if (ctx.args.isNotEmpty() && ctx.args[0].toIntOrNull() == null) {
            return
        }

        if (ctx.args.isEmpty() || ctx.args[0] == "1") {
            player!!.nextTrack()
        } else {
            val track = player!!.queue.removeAt(ctx.args[0].toInt() - 1)
            for (i in 0 until ctx.args[0].toInt()) {
                player.queue.removeAt(i)
            }
            player.lastTrack = player.currentTrack
            player.player.playTrack(track)
        }

        ctx.send("Skipped to the song.")
    }
}