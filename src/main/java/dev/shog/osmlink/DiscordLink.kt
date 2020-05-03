package dev.shog.osmlink

import dev.shog.osmlink.bot.getBot
import dev.shog.osmlink.handle.DataHandler
import dev.shog.osmlink.handle.MessageHandler
import discord4j.core.GatewayDiscordClient
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

/**
 * Main class
 */
open class DiscordLink : JavaPlugin() {
    companion object {
        const val VERSION = 1.2f
        var CLIENT: GatewayDiscordClient? = null
    }

    override fun onEnable() {
        println("DiscordLink: Enabled (v$VERSION)")

        configuration.load()

        if (configuration.getString("url") == null) {
            configuration.setProperty("url", "")
            configuration.setProperty("botToken", "")
            configuration.setProperty("discordChannel", "670055234416017430")
            configuration.save()

            System.err.println("DiscordLink: Please fill out the config file!")

            return
        }

        getCommand("discordlink").setExecutor { sender, command, s, args ->
            when {
                args.isEmpty() -> {
                    sender?.sendMessage("${ChatColor.YELLOW}DiscordLink: ${ChatColor.GOLD}v${VERSION}")
                }

                args[0].equals("reload", true) && (sender.isOp || sender.hasPermission("osm.dl.reload")) -> {
                    configuration.load()

                    sender.sendMessage("${ChatColor.YELLOW}The config has been reloaded.")
                }
            }

            return@setExecutor true
        }

        getCommand("cursed").setExecutor { sender, _, _, args ->
            val cursed = DataHandler.getCursed()

            when {
                args.isEmpty() -> {
                    sender.sendMessage(
                        MessageHandler.getMessage(
                            "commands.cursed.default", cursed.asSequence().joinToString(", ").removeSuffix(", ")
                    ))

                    return@setExecutor true
                }

                args.isNotEmpty() && args.size == 2 -> {
                    when (args[0].toLowerCase()) {
                        "add" -> {
                            val word = args[1]

                            if (cursed.contains(word.toLowerCase())) {
                                sender.sendMessage(MessageHandler.getMessage("commands.cursed.already-exists"))
                            } else {
                                DataHandler.addCursed(word)
                                sender.sendMessage(MessageHandler.getMessage("commands.cursed.added", word))
                            }

                            return@setExecutor true
                        }

                        "remove" -> {
                            val word = args[1]

                            if (!cursed.contains(word.toLowerCase())) {
                                sender.sendMessage(MessageHandler.getMessage("commands.cursed.doesnt-exist"))
                            } else {
                                DataHandler.removeCursed(word)
                                sender.sendMessage(MessageHandler.getMessage("commands.cursed.removed", word))
                            }

                            return@setExecutor true
                        }
                    }
                }
            }

            return@setExecutor false
        }

        CLIENT = getBot().getClient()
    }

    override fun onDisable() {
        println("DiscordLink: Disabled")
    }
}