package xyz.oniverse.music.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.VoiceChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.oniverse.music.Oni
import xyz.oniverse.music.audio.entities.TrackData
import xyz.oniverse.music.listeners.framework.Context

class ExtendedResultHandler(
    private val id: String?,
    private val ctx: Context,
    private val trackData: TrackData,
    private val musicManager: ExtendedMusicManager,
    private val playNext: Boolean = false
) : AudioLoadResultHandler {
    private var retryCount = 0

    private val log: Logger = LoggerFactory.getLogger("result-handler")
    private var isFromPlaylist = false

    override fun trackLoaded(track: AudioTrack?) {
        val isNextPlay = musicManager.queue.isEmpty() && musicManager.player.playingTrack == null

        if (!checkVoiceState() || track == null) return

        track.userData = trackData

        if (!isNextPlay) {
            if (!isFromPlaylist) {
                ctx.embed {
                    setColor(ctx.color)
                    setDescription("Added `${track.info.title}`")
                }
            }
        }

        if (!isFromPlaylist) musicManager.queue(track, playNext)
    }

    override fun playlistLoaded(playlist: AudioPlaylist?) {
        if (playlist!!.isSearchResult) {
            return trackLoaded(playlist.tracks.first())
        }

        isFromPlaylist = true

        for (track in playlist.tracks) {
            musicManager.queue(track, playNext)
            track.userData = trackData
        }

        trackLoaded(playlist.tracks.first())

        ctx.embed {
            setColor(ctx.color)
            setDescription("Added playlist `${playlist.name}` to the queue")
        }
    }

    override fun noMatches() {
        if (retryCount < MAX_LOAD_RETRIES && id != null) {
            retryCount++
            Oni.player.playerManager.loadItemOrdered(ctx.guild.idLong, id, this)
            return
        }

        if (musicManager.isIdle) {
            musicManager.destroy()
        }

        val shownId: String = if (id == "") "unknown"
        else id.toString()
        ctx.embed {
            setTitle("No matches")
            setDescription("No matches were found for `$shownId`!")
        }
    }

    override fun loadFailed(exception: FriendlyException) {
        if (retryCount != MAX_LOAD_RETRIES && id != null) {
            retryCount++
            Oni.player.playerManager.loadItemOrdered(ctx.guild.idLong, id, this)
            return
        }

        if (musicManager.isIdle) {
            musicManager.destroy()
        }

        println(exception.message)

        ctx.send("Sorry, I'm unable to load the track at this time! :(")
    }

    private fun checkVoiceState(): Boolean {
        val manager = ctx.guild.audioManager

        if (manager.connectedChannel == null) {
            if (ctx.vc == null) {
                ctx.embed {
                    setColor(ctx.color)
                    setDescription("You left before the track was loaded.")
                }

                if (musicManager.isIdle) {
                    musicManager.destroy()
                }

                return false
            }

            return musicManager.openAudioConnection(ctx.vc as VoiceChannel, ctx)
        }

        return true
    }

    companion object {
        private const val MAX_LOAD_RETRIES = 1

        fun loadItem(identifier: String, ctx: Context, trackData: TrackData,
                     musicManager: ExtendedMusicManager, playNext: Boolean = false) {
            val extendedResultHandler = ExtendedResultHandler(identifier, ctx, trackData, musicManager, playNext)
            Oni.player.playerManager.loadItemOrdered(ctx.guild, identifier, extendedResultHandler)
        }
    }
}