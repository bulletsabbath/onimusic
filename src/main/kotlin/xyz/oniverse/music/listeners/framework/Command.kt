package xyz.oniverse.music.listeners.framework

import xyz.oniverse.music.audio.ExtendedMusicManager
import xyz.oniverse.music.entities.Reply

abstract class Command {

    lateinit var name: String
    var aliases = listOf<String>()
    var description: String? = "No description"
    var usage: String? = "No usage listed"
    var category = CommandCategory.DEV

    abstract fun execute(ctx: Context, reply: Reply)

    fun check(ctx: Context, reply: Reply, manager: ExtendedMusicManager?): Boolean {
        if (ctx.vc == null) {
            ctx.send(reply["user-not-in-channel"])
            return false
        }

        if (ctx.vc != ctx.guild.selfMember.voiceState?.channel) {
            ctx.send(reply["different-channels"])
            return false
        }

        if (manager?.player?.playingTrack == null) {
            ctx.send(reply["no-song-playing"])
            return false
        }

        return true
    }

    enum class CommandCategory {
        MUSIC,
        MISC,
        DEV
    }
}