package xyz.oniverse.music.commands.music

import xyz.oniverse.music.Oni
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context

class PlayNext : Command() {
    init {
        name = "playnext"
        aliases = listOf("pn", "playtop")
        usage = "${Oni.config.prefix}playnext {query} (for ytmusic) -m/--music"
        category = CommandCategory.MUSIC
    }

    override fun execute(ctx: Context, reply: Reply) {
        val manager = Oni.player.getExisting(ctx.guild.idLong)
            ?: return ctx.send(reply["no-player"])

        val botChannel = ctx.guild.selfMember.voiceState?.channel
            ?: return ctx.send(reply["bot-not-in-channel"])
        val userChannel = ctx.vc ?: return ctx.send(reply["user-not-in-channel"])

        if (botChannel != userChannel) {
            return ctx.send(reply["song-playing-elsewhere"])
        }

        if (ctx.args.isNotEmpty())
            Play.play(ctx, ctx.args, manager, true)
        else if (manager.player.isPaused)
            manager.player.isPaused = false
        else
            ctx.send(reply["no-input"])
    }
}