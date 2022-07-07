package xyz.oniverse.music.commands.music

import xyz.oniverse.music.Oni
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context

class Previous : Command() {

    init {
        name = "previous"
        aliases = listOf("pr", "prvs")
        description = "plays the previous song again!"
        category = CommandCategory.MUSIC
        usage = "${Oni.config.prefix}previous"
    }


    override fun execute(ctx: Context, reply: Reply) {
        if (ctx.vc == null) return ctx.send(reply["user-not-in-channel"])
        val player = Oni.player.getExisting(ctx.guild.idLong)

        if (player?.lastTrack == null)
            return ctx.send("I don't think any song has played before 0_0")

        player.currentTrack?.let { player.queue(it, true) }
        player.player.playTrack(player.lastTrack!!.makeClone())
    }
}