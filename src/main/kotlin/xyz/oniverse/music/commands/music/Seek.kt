package xyz.oniverse.music.commands.music

import xyz.oniverse.music.Oni
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context

class Seek : Command() {

    init {
        name = "seek"
        aliases = listOf("forward", "seekto")
        description = "Goes to a specific second of the song!"
        category = CommandCategory.MUSIC
        usage = "${Oni.config.prefix}seek {hours}:{minutes}:{seconds}"
    }

    override fun execute(ctx: Context, reply: Reply) {
        val player = Oni.player.getExisting(ctx.guild.idLong) ?: return ctx.send(reply["no-player"])
        if (ctx.args.isEmpty()) ctx.send("You have to give me a position to go to!")

        val matcher = POSITION_PATTERN.matcher(ctx.args[0])
        if (!matcher.matches()) return ctx.send("That's not a valid position! The pattern is {hours}:{minutes}:{seconds}")
        val track = player.player.playingTrack ?: return ctx.send(reply["no-song-playing"])

        val time = matcher.group("hour").toInt().times(3600000).plus(matcher.group("minute").toInt().times(60000)).plus(matcher.group("second").toInt().times(1000))
        if (time > track.duration) {
            return ctx.send("That is not a valid position in the song!")
        }
        val newPosition = Math.max(0, Math.min(track.position + time, track.duration - 1))
        track.position = newPosition
    }

    companion object {
        private val POSITION_PATTERN = "(?<hour>[0-9][0-9]):(?<minute>[0-5][0-9]):(?<second>[0-5][0-9])".toPattern()
    }
}