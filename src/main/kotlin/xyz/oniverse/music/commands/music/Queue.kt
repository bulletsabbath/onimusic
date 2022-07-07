package xyz.oniverse.music.commands.music

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import xyz.oniverse.music.Oni
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context
import kotlin.math.ceil

class Queue : Command() {

    init {
        name = "queue"
        aliases = listOf("q")
        description = "Shows the queue"
        category = CommandCategory.MUSIC
    }
    val array: ArrayList<String> = ArrayList()
    var page = 0

    override fun execute(ctx: Context, reply: Reply) {
        if (ctx.vc == null) return ctx.send(reply["user-not-in-channel"])
        val manager = Oni.player.getExisting(ctx.guild.idLong) ?: return ctx.send(reply["no-queue"])

        val queue = manager.queue

        if (queue.isEmpty()) {
            return ctx.send(reply["nothing-in-queue"])
        }

        array.clear()

        val length = ceil((queue.size / 10).toDouble()).toInt()
        //why is this telling me to convert it twice lol
        for (i in 0 .. length) {
            var string = ""
            if (i == 0) {
                string += "**Now playing:** [${manager.player.playingTrack.info.title}" +
                        "](${manager.player.playingTrack.info.uri})\n\n"
            }
            val to = if (i * 10 + 10 <= queue.size) i * 10 + 10 else queue.size
            val tracks = queue.subList(i * 10, to)

            var j = i * 10 + 1
            for (track in tracks) {
                string += "**${j++}.** [${track.info.title}" +
                        "](${track.info.uri})"

                val minutes = if (track.info.length/60000 < 10) "0${track.info.length/60000}" else track.info.length/60000
                val seconds = if ((track.info.length/1000) % 60 < 10) "0${((track.info.length/1000) % 60)}" else ((track.info.length/1000) % 60)

                string += " `[$minutes:$seconds]`\n"
            }
            array.add(string)
        }
        ctx.channel.sendMessageEmbeds(
            EmbedBuilder()
                .setDescription(array[0])
                .setFooter("Page 1/${array.size}")
                .setColor(Oni.config.color)
                .build()
        ).setActionRow(
            Button.primary("first", Emoji.fromUnicode("U+23EE")),
            Button.primary("previous", Emoji.fromUnicode("U+23EA")),
            Button.primary("next", Emoji.fromUnicode("U+23E9")),
            Button.primary("last", Emoji.fromUnicode("U+23ED"))
        ).queue()
    }



    companion object {

        fun firstOnQueue(event: ButtonInteractionEvent, array: ArrayList<String>) {
            try {
                event.message.editMessageEmbeds(
                    EmbedBuilder()
                        .setColor(Oni.config.color)
                        .setDescription(array.first())
                        .setFooter("Page 1/${array.size}")
                        .build()
                ).queue()
            } catch (e: Exception) {
                println(e.message)
            }

        }

        fun previousOnQueue(event: ButtonInteractionEvent, page: Int, array: ArrayList<String> ) {
            val i = page - 1
            if (i >= array.size || i <= 0) return
            event.message.editMessageEmbeds(
                EmbedBuilder()
                    .setColor(Oni.config.color)
                    .setDescription(array[i - 1])
                    .setFooter("Page ${i}/${array.size}")
                    .build()
            ).queue()

        }

        fun nextOnQueue(event: ButtonInteractionEvent, page: Int, array: ArrayList<String>) {
            val i = page + 1
            println(page)
            println(i)
            if (i >= array.size || i < 0) return
            event.message.editMessageEmbeds(
                EmbedBuilder()
                    .setColor(Oni.config.color)
                    .setDescription(array[i - 1])
                    .setFooter("Page ${i}/${array.size}")
                    .build()
            ).queue()
        }

        fun lastOnQueue(event: ButtonInteractionEvent, array: ArrayList<String>) {
            event.message.editMessageEmbeds(
                EmbedBuilder()
                    .setColor(Oni.config.color)
                    .setDescription(array.last())
                    .setFooter("Page ${array.size}/${array.size}")
                    .build()
            ).queue()
        }
    }
}