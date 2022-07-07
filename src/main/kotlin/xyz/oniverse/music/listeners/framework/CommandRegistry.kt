package xyz.oniverse.music.listeners.framework

import xyz.oniverse.music.Oni

class CommandRegistry(cmds: ArrayList<Command>) {

    val commands = arrayListOf<Command>()

    init {
        cmds.forEach {
            registerCommand(it)
        }
    }

    private fun registerCommand(cmd: Command) {
        val allAliases = ArrayList<String>()
        allAliases.add(cmd.name)
        allAliases.addAll(cmd.aliases)
        for (trigger in allAliases) {
            if (getCommand(trigger) != null) {
                Oni.log.info("Duplicate command trigger '$trigger' in the command registry.")
            }
        }
        commands.add(cmd)
    }

    fun getCommand(cmd: String): Command? {
        return commands.stream()
            .filter {
                cmd.equals(it.name, ignoreCase = true) || it.aliases.contains(cmd)
            }
            .findFirst()
            .orElse(null)
    }
}