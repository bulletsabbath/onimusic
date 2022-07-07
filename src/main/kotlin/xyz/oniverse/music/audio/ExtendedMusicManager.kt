package xyz.oniverse.music.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.VoiceChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.oniverse.music.Oni
import xyz.oniverse.music.audio.entities.TrackData
import xyz.oniverse.music.listeners.framework.Context
import java.nio.ByteBuffer
import java.util.*

class ExtendedMusicManager(guildId: Long, val player: AudioPlayer) : AudioSendHandler, AudioEventAdapter() {

    private val log: Logger = LoggerFactory.getLogger("music-manager")

    val isAlone: Boolean get() = guild?.selfMember?.voiceState?.channel?.members?.none { !it.user.isBot } ?: true
    val isIdle: Boolean get() = player.playingTrack == null && queue.isEmpty()

    var currentTrack: AudioTrack? = null
        private set
    var lastTrack: AudioTrack? = null

    val queue = LinkedList<AudioTrack>()

    private val guild = Oni.jda.getGuildById(guildId)
    val musicChannel get() =
        (player.playingTrack ?: lastTrack)?.getUserData(TrackData::class.java)?.requestedChannel?.let { guild?.getTextChannelById(it) }

    init {
        player.addListener(this)
    }

    fun nextTrack() {
        if (queue.isNotEmpty()) {
            val track = queue.poll()

            if (track != null) {
                lastTrack = currentTrack
                currentTrack = track
                return player.playTrack(track)
            }
        }
        currentTrack = null
        return player.stopTrack()
    }

    private fun nowPlaying(track: AudioTrack) {
        val desc = buildString {
            append("Now playing `").append(track.info.title)

            val requester = track.getUserData(TrackData::class.java)?.requester?.let { guild?.getMemberById(it)?.asMention }
            append("`, requested by ").append(requester ?: "`unknown user`").append("")
        }

        musicChannel?.sendMessageEmbeds(
            EmbedBuilder()
                .setTitle("Now Playing")
                .setDescription(desc)
                .setColor(Oni.config.color)
                .build()
        )?.queue()
    }

    fun queue(track: AudioTrack, isNext: Boolean) {
        if (!player.startTrack(track, true)) {
            if (isNext) {
                queue.addFirst(track)
            } else {
                queue.offer(track)
            }
        }
    }

    fun openAudioConnection(channel: VoiceChannel, ctx: Context): Boolean {
        when {
            !guild?.selfMember!!.hasPermission(channel, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK) -> {
                ctx.send("Can't join or talk in `${channel.name}`!")
                return false
            }
            channel.userLimit != 0 && channel.members.size >= channel.userLimit
                    && !guild.selfMember.hasPermission(Permission.VOICE_MOVE_OTHERS) -> {
                ctx.send("The voice channel is full! You need to give me permissions or raise the channel limit.")
                return false
            }
            else -> {
                guild.audioManager.apply {
                    openAudioConnection(channel)
                    sendingHandler = this@ExtendedMusicManager
                    isSelfDeafened = true
                }
                return true
            }
        }
    }

    fun moveAudioConnection(channel: VoiceChannel) {
        guild?.let {
            if (it.selfMember.voiceState!!.channel == null) {
                destroy()
                return
            }

            if (!it.selfMember.hasPermission(channel, Permission.VOICE_CONNECT)) {
                musicChannel?.sendMessage("I cannot join ${channel.name}!")?.queue()
                destroy()
                return
            }

            player.isPaused = true
            it.audioManager.openAudioConnection(channel)
            it.audioManager.isSelfDeafened = true
            player.isPaused = false

            musicChannel?.sendMessageEmbeds(EmbedBuilder().apply {
                setColor(0x6F6F8F)
                setTitle("Moving Channels")
                setDescription("Moving to `${channel.name}`.")
            }.build())?.queue()
        }
    }

    fun closeAudioConnection(channel: VoiceChannel) {
        guild?.audioManager?.apply {
            closeAudioConnection()
            queue.clear()
            sendingHandler = null
        }
    }

    fun destroy() = Oni.player.destroy(guild!!.idLong)

    /**
     * event handling
     */

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack) {
        currentTrack = track
        nowPlaying(track)
    }

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason) {

        if (endReason.mayStartNext) {
            lastTrack = track
            nextTrack()
        }
    }

    override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack?, thresholdMs: Long) {
        musicChannel?.sendMessageEmbeds(
            EmbedBuilder()
                .setColor(0x6F6F8F)
                .setTitle("Track Stuck")
                .setDescription("An error occurred! The track has been stuck for longer than $thresholdMs ms. " +
                        "Playing next track.")
                .build())?.queue()

        nextTrack()
    }

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException?) {
        log.error("Exception in track: $exception")
    }

    override fun onPlayerPause(player: AudioPlayer?) {
        musicChannel?.sendMessage("Player paused.")?.queue()
    }

    override fun onPlayerResume(player: AudioPlayer?) {
        musicChannel?.sendMessage("Player unpaused.")?.queue()
    }

    /**
     * send handler
     **/
    private val buffer = ByteBuffer.allocate(1024)
    private val frame = MutableAudioFrame().apply { setBuffer(buffer) }

    override fun canProvide() = player.provide(frame)
    override fun provide20MsAudio(): ByteBuffer? = buffer.flip()
    override fun isOpus() = true
}