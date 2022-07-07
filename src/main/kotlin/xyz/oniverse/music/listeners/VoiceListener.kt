package xyz.oniverse.music.listeners

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import xyz.oniverse.music.Oni
import xyz.oniverse.music.audio.PlayerHandler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class VoiceListener(private val handler: PlayerHandler) : ListenerAdapter() {

    private val future = CompletableFuture<Unit>()

    override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        if (event.member.idLong == event.jda.selfUser.idLong) {
            if (!event.guild.selfMember.voiceState!!.isDeafened) {
                event.guild.selfMember.deafen(true)
            }
        }
    }

    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        val manager = handler.get(event.guild.idLong)
        if (event.member.idLong == event.jda.selfUser.idLong) {
            handler.destroy(event.guild.idLong)
            return
        }
        if (manager.isIdle) {
            Executors.newCachedThreadPool().submit {
                Thread.sleep(10000)
                manager.musicChannel!!.sendMessageEmbeds(
                    EmbedBuilder()
                        .setColor(Oni.config.color)
                        .setTitle("Channel left")
                        .setDescription("Leaving channel because of idleness to save resources.")
                        .build()
                ).queue()
                future.complete(manager.closeAudioConnection(event.channelLeft as VoiceChannel))
                return@submit
            }
            return future.get()
        }

        if (manager.isAlone) {
            Executors.newCachedThreadPool().submit {
                Thread.sleep(10000)
                future.complete(manager.closeAudioConnection(event.channelLeft as VoiceChannel))
                return@submit
            }
            return future.get()
        }
    }

    override fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
        when (event.member.idLong) {
            event.guild.selfMember.idLong -> {
                handler.get(event.guild.idLong).moveAudioConnection(event.newValue as VoiceChannel)
            }
        }
    }

    override fun onGuildVoiceGuildDeafen(event: GuildVoiceGuildDeafenEvent) {
        if (event.member.idLong == event.guild.selfMember.idLong
            && !event.member.voiceState!!.isDeafened) {
            //this doesn't even work?
            Oni.player.getExisting(event.guild.idLong)!!.musicChannel?.
            sendMessage("Don't undeafen me please! ;^;")!!.queue()
            event.guild.selfMember.deafen(true)
        }
    }
}