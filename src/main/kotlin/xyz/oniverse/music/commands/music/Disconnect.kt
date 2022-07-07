package xyz.oniverse.music.commands.music

import net.dv8tion.jda.api.entities.VoiceChannel
import xyz.oniverse.music.Oni
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context

class Disconnect : Command() {

    init {
        name = "disconnect"
        aliases = listOf("goaway", "dc")
        description = "Leaves the current channel"
        category = CommandCategory.MUSIC
        usage = "${Oni.config.prefix}disconnect"
    }

    override fun execute(ctx: Context, reply: Reply) {
        val player = Oni.player.getExisting(ctx.guild.idLong) ?: return ctx.send(reply["no-player"])
        val botChannel = ctx.guild.selfMember.voiceState?.channel
        val userChannel = ctx.vc ?: return ctx.send(reply["user-not-in-channel"])

        if (userChannel != botChannel) {
            return ctx.send("song-playing-elsewhere")
        }

        player.closeAudioConnection(ctx.vc as VoiceChannel)
    }
}