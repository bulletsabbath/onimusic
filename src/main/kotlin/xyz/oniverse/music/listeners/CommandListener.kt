package xyz.oniverse.music.listeners

import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.oniverse.music.Oni
import xyz.oniverse.music.commands.music.Queue
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.CommandRegistry
import xyz.oniverse.music.listeners.framework.Context
import java.util.regex.Pattern

class CommandListener(Oni: Oni) : ListenerAdapter() {

    val log: Logger = LoggerFactory.getLogger("listener")

    val oni = Oni

    override fun onReady(event: ReadyEvent) {
        log.info("Logged in as ${oni.jda.selfUser.name}#${oni.jda.selfUser.discriminator}.")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val message = event.message

        if (event.author.isBot) return

        if (message.isFromType(ChannelType.TEXT) && message.contentRaw.startsWith(oni.config.prefix, ignoreCase = true)) {

            val arr = message.contentRaw.substring(oni.config.prefix.length).trim().split(Pattern.compile("\\s+"))
            val args = arr.subList(1, arr.size)
            val cmdString = arr.first()
            val ctx = Context(event, args)

            try {
                val command: Command = registry.getCommand(cmdString) ?: return

                if (command.category == Command.CommandCategory.DEV && message.author.id != "491266855798046722") {
                    return
                }

                command.execute(ctx, oni.reply)
            } catch (e: Exception) {
                log.error("${e.cause} ${e.printStackTrace()}")
                ctx.send("Sorry, I can't execute your command right now!")
            }

        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val cmd: Queue = (registry.getCommand("queue") as Queue?)!!
        when (event.componentId) {
            "first" -> {
                try {
                    Queue.firstOnQueue(event, cmd.array)
                } catch (ignored: Exception) {
                    return
                }
                cmd.page = 0
            }
            "previous" -> {
                try {
                    Queue.previousOnQueue(event, cmd.page, cmd.array)
                } catch (ignored: Exception) {
                    return
                }
                cmd.page--
            }
            "next" -> {
                try {
                    Queue.nextOnQueue(event, cmd.page, cmd.array)
                } catch (ignored: Exception) {
                    return
                }
                cmd.page++
            }
            "last" -> {
                try {
                    Queue.lastOnQueue(event, cmd.array)
                } catch (ignored: Exception) {
                    return
                }
                cmd.page = cmd.array.lastIndex
            }
        }
        try {
            event.reply(" ").queue()
        } catch (ignored: Exception) { //ignore
            log.info("")
        }
    }

    companion object {
        val registry = CommandRegistry(Oni.commands)
    }
}