package xyz.oniverse.music.commands.music

import xyz.oniverse.music.Oni
import xyz.oniverse.music.audio.ExtendedMusicManager
import xyz.oniverse.music.audio.ExtendedResultHandler
import xyz.oniverse.music.audio.entities.TrackData
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context

class Play : Command() {

    init {
        name = "play"
        aliases = listOf("p")
        description = "Plays the song you want!"
        category = CommandCategory.MUSIC
    }

    override fun execute(ctx: Context, reply: Reply) {
        val botChannel = ctx.guild.selfMember.voiceState?.channel
        val userChannel = ctx.vc ?: return ctx.send(reply["user-not-in-channel"])

        if (botChannel != null && botChannel != userChannel) {
            return ctx.send(reply["song-playing-elsewhere"])
        }

        val manager = Oni.player.get(ctx.guild.idLong)

        if (ctx.args.isNotEmpty()) {
            return play(ctx, ctx.args, manager)
        }

        manager.player.isPaused = false
    }

    companion object {
        fun play(ctx: Context, args: List<String>, manager: ExtendedMusicManager, isNext: Boolean = false) {

            val isYtMusic = args.lastOrNull{ it == "--music" || it == "-m" }

            val filteredArgs = isYtMusic?.let {
                val argumentIndex = args.lastIndexOf(isYtMusic)
                args.filterIndexed { index, _ -> index != argumentIndex }
            } ?: args

            val query = when {
                "https://" in args[0] || "http://" in args[0] -> {
                    args[0].removePrefix("<").removeSuffix(">")
                }
                else -> {
                    if (isYtMusic != null) {

                        "ytmsearch:${filteredArgs.joinToString(" ").trim()}"
                    } else {
                        "ytsearch:${filteredArgs.joinToString(" ").trim()}"
                    }
                }
            }

            val trackData = TrackData(ctx.author.idLong, ctx.channel.idLong)

            ExtendedResultHandler.loadItem(query, ctx, trackData, manager, isNext)
        }
    }
}