package xyz.oniverse.music.commands.music

import xyz.oniverse.music.Oni
import xyz.oniverse.music.audio.entities.TrackData
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context

class NowPlaying : Command() {

    init {
        name = "nowplaying"
        aliases = listOf("np")
        description = "Shows the current song!"
        category = CommandCategory.MUSIC
    }

    override fun execute(ctx: Context, reply: Reply) {
        val player = Oni.player.getExisting(ctx.guild.idLong)

        if (!check(ctx, reply, player)) return

        val track = player!!.player.playingTrack
        val duration = track.duration
        val current = track.position

        val currentMinutes = if (current / 60000 < 10) "0${current / 60000}" else current /60000
        val currentSeconds = if ((current /1000) % 60 < 10) "0${((current /1000) % 60)}" else ((current /1000) % 60)

        val minutes = if (duration/60000 < 10) "0${duration /60000}" else duration /60000
        val seconds = if ((duration /1000) % 60 < 10) "0${((duration /1000) % 60)}" else ((duration /1000) % 60)

            var length = buildString {

                append("[").append("${currentMinutes}:${currentSeconds}").append(" / ").append("${minutes}:${seconds}").append("]")
            }

        if (track.info.isStream) length = "`STREAM`"

        val trackData = track.userData as TrackData
        ctx.embed {
            setTitle("Now Playing")
            setDescription("[${track.info.title}](${track.info.uri})\n`${length}`\n\nRequested by: ${ctx.guild.getMemberById(trackData.requester)?.asMention}")
        }
    }
}