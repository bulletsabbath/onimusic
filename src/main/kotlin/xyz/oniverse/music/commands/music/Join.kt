package xyz.oniverse.music.commands.music

import net.dv8tion.jda.api.entities.VoiceChannel
import xyz.oniverse.music.Oni
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context

class Join : Command() {
    init {
        name = "join"
        aliases = listOf("j", "summon", "sm")
        description = "Join the voice channel you are in."
        category = CommandCategory.MUSIC
    }

    override fun execute(ctx: Context, reply: Reply) {
        val musicManager = Oni.player.get(ctx.guild.idLong)

        if (ctx.vc == null) return ctx.send(reply["user-not-in-channel"])

        val channel = ctx.vc as VoiceChannel

        if (ctx.guild.audioManager.connectedChannel != null) {
            musicManager.moveAudioConnection(channel)
        } else {
            musicManager.openAudioConnection(channel, ctx)
        }

    }
}