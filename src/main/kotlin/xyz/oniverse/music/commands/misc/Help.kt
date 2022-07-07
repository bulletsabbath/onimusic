package xyz.oniverse.music.commands.misc

import net.dv8tion.jda.api.EmbedBuilder
import xyz.oniverse.music.Oni
import xyz.oniverse.music.entities.Reply
import xyz.oniverse.music.listeners.framework.Command
import xyz.oniverse.music.listeners.framework.Context
import xyz.oniverse.music.listeners.CommandListener

class Help : Command() {

    init {
        name = "help"
        aliases = listOf("helpme", "howto", "how")
        description = "help command!"
        category = CommandCategory.MISC
        usage = "${Oni.config.prefix}help [command]"

    }

    override fun execute(ctx: Context, reply: Reply) {

        if (ctx.args.isNotEmpty()) {
            CommandListener.registry.getCommand(ctx.args[0]).let {
                if (it == null) return ctx.send("I can't seem to find that command.")

                if (it.category == CommandCategory.DEV) return
                val aliases = buildString {
                    append("`").append(it.aliases.joinToString("`, `")).append("`")
                }
                val embed = EmbedBuilder()
                    .setColor(Oni.config.color)
                    .addField("name", it.name, true)
                    .addField("aliases", aliases, true)
                    .addField("description", it.description, true)
                    .addField("usage", it.usage, false)

                ctx.send(embed)
            }
        } else {
            val string = buildString {
                for (command in CommandListener.registry.commands) {
                    if (command.category == CommandCategory.DEV) continue
                    append("`${command.name}` - ${command.usage}\n")
                }
            }

            val embed = EmbedBuilder()
                .setColor(Oni.config.color)
                .setDescription(string)
                .setFooter("use ${Oni.config.prefix}help [command] to learn more about a command")
            ctx.send(embed)
        }
    }
}