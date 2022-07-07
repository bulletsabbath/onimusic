package xyz.oniverse.music.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class AudioPlayerSendHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
    private val buffer = ByteBuffer.allocate(1024)
    var frame = MutableAudioFrame().apply { setBuffer(buffer) }

    override fun canProvide() = audioPlayer.provide(frame)
    override fun provide20MsAudio(): ByteBuffer? = buffer.flip()
    override fun isOpus() = true
}