package xyz.oniverse.music.listeners.framework

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import xyz.oniverse.music.Oni

class Context(event: MessageReceivedEvent, val args: List<String>) {

    val message = event.message
    val author = event.author
    val member = event.member
    val channel = event.channel
    val jda = event.jda
    val guild = event.guild
    val vc = member!!.voiceState?.channel

    val color = Oni.config.color

    /** reply stuff **/

    fun send(msg: String) {
        channel.sendMessage(msg).queue()
    }

    fun send(embed: EmbedBuilder) {
        channel.sendMessageEmbeds(embed.build()).queue()
    }

    fun embed(title: String, desc: String) {
        embed {
            setTitle(title)
            setDescription(desc)
        }
    }

    fun embed(block: EmbedBuilder.() -> Unit) {
        val embed = EmbedBuilder()
            .setColor(color)
            .apply(block)
            .build()

        channel.sendMessageEmbeds(embed).queue()
    }
}