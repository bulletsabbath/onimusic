package xyz.oniverse.music

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.oniverse.music.audio.PlayerHandler
import xyz.oniverse.music.commands.misc.Help
import xyz.oniverse.music.commands.misc.Ping
import xyz.oniverse.music.commands.music.*
import xyz.oniverse.music.entities.Config
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.CommandListener
import xyz.oniverse.music.listeners.VoiceListener
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

object Oni {

    val config = Config.load()
    val reply = Reply.load()

    val player = PlayerHandler()

    var commands = arrayListOf(
        Ping(),
        Help(),
        Play(),
        Skip(),
        Join(),
        Queue(),
        PlayNext(),
        Disconnect(),
        NowPlaying(),
        MoveSong(),
        Remove(),
        Shuffle(),
        Previous(),
        Seek(),
        Clear()
    )

    val log: Logger = LoggerFactory.getLogger("main")

    lateinit var jda: JDA

    @JvmStatic
    fun main(args: Array<String>) {

        AudioSourceManagers.registerRemoteSources(player.playerManager)

        log.info("Baking a cake...")

        log.info("Loading JDA...")

        val gatewayPool: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
        val rateLimitPool: ScheduledExecutorService = Executors.newScheduledThreadPool(2)

        jda = JDABuilder.create(
            config.token,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_MEMBERS,
        )
            .addEventListeners(CommandListener(this), VoiceListener(player))
            .disableIntents(
                //annoying
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_BANS,
                GatewayIntent.GUILD_MESSAGE_TYPING,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_INVITES
            )
            .setGatewayPool(gatewayPool)
            .setRateLimitPool(rateLimitPool)
            .build()

        log.info("Loaded JDA")
    }
}