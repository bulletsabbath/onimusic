package xyz.oniverse.music.audio

import java.util.concurrent.ConcurrentHashMap

class PlayerHandler {

    val playerManager = ExtendedAudioPlayerManager()
    private val registry = ConcurrentHashMap<Long, ExtendedMusicManager>()

    fun get(guild: Long) =  registry.computeIfAbsent(guild) { ExtendedMusicManager(guild, playerManager.createPlayer()) }
    fun getExisting(guild: Long) = registry[guild]
    fun destroy(guild: Long) = registry.remove(guild)
}