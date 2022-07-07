package xyz.oniverse.music.audio

import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifyConfig
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifySourceManager
import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.oniverse.music.Oni
import xyz.oniverse.music.audio.entities.TrackData
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class ExtendedAudioPlayerManager(private val dapm: DefaultAudioPlayerManager = DefaultAudioPlayerManager()) : DefaultAudioPlayerManager() {

    private val log: Logger = LoggerFactory.getLogger("player-manager")

    init {
        val credentials = Oni.config

        val spotifyConfig = SpotifyConfig()
        spotifyConfig.clientId = credentials.clientId
        spotifyConfig.clientSecret = credentials.clientSecret
        spotifyConfig.setCountryCode("US")



        val yasm = YoutubeAudioSourceManager(true, "thisisan@example.lol", "example")
        if (credentials.clientId.isNotEmpty() && credentials.clientSecret.isNotEmpty()) {
            registerSourceManager(SpotifySourceManager(null, spotifyConfig, this))
            log.info("Registered Spotify source manager.")
        }
        registerSourceManagers(
            yasm,
            SoundCloudAudioSourceManager.createDefault()
        )

        log.info("Registered all source managers.")
    }

    private fun registerSourceManagers(vararg sourceManagers: AudioSourceManager) {
        // source? trust me bro
        for (source in sourceManagers) {
            registerSourceManager(source)
        }
    }

    fun toBase64String(track: AudioTrack): String {
        return ByteArrayOutputStream().use { baos ->
            encodeTrack(MessageOutput(baos), track)

            track.userData.takeIf { it is TrackData }.let {
                (it as TrackData?)?.serialize(baos)
            }

            Base64.getEncoder().encodeToString(baos.toByteArray())
        }
    }

    private fun toAudioTrack(encodedTrack: String): AudioTrack? {

        val decoded = Base64.getDecoder().decode(encodedTrack)
        val bais = ByteArrayInputStream(decoded)
        val track = bais.use {
            decodeTrack(MessageInput(it)).decodedTrack
        }

        val audioTrack = track ?: return null

        val trackData = TrackData.deserialize(bais)
        if (trackData != null) {
            audioTrack.userData = trackData
        }
        return audioTrack
    }

    fun decodePlaylist(encodedTracks: List<String>, name: String): BasicAudioPlaylist {
        val decoded = encodedTracks.mapNotNull(::decodeAudioTrackOrNull)
        return BasicAudioPlaylist(name, decoded, decoded[0], false)
    }

    fun decodePlaylist(jsonString: String): BasicAudioPlaylist {
        val jo = JSONObject(jsonString)

        val name = jo.getString("name")
        val isSearch = jo.getBoolean("search")
        val selected = jo.getInt("selected")

        val encodedTracks = jo.getJSONArray("tracks")
        val tracks = mutableListOf<AudioTrack>()

        for (encodedTrack in encodedTracks) {
            val decodedTrack = decodeAudioTrack(encodedTrack as String)
            tracks.add(decodedTrack)
        }

        val selectedTrack = if (selected > -1) tracks[selected] else null

        return BasicAudioPlaylist(name, tracks, selectedTrack, isSearch)
    }

    fun decodeAudioTrack(base64: String) = toAudioTrack(base64)!!
    private fun decodeAudioTrackOrNull(base64: String) = toAudioTrack(base64)
}