package dev.shog.osmlink.bot

import dev.shog.osmlink.DiscordLink
import dev.shog.osmlink.execCommands
import dev.shog.osmlink.getProperContent
import dev.shog.osmlink.handle.DataHandler
import dev.shog.osmlink.handle.MessageHandler
import dev.shog.osmlink.handle.WebhookHandler
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import me.moderator_man.fo.FakeOnline
import org.bukkit.entity.Player
import dev.shog.osmpl.api.data.DataManager
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityListener
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerListener
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Get the bot.
 */
fun DiscordLink.getBot() = object : IBot {
    private val CLIENT: GatewayDiscordClient = DiscordClient
            .create(configuration.getString("botToken"))
            .gateway()
            .login()
            .doOnNext {
                it.on(MessageCreateEvent::class.java)
                        .filter { e -> e.message.author.isPresent && e.member.isPresent }
                        .filterWhen { e ->
                            e.client.self
                                    .map { user -> user.id }
                                    .map { id -> e.member.get().id != id }
                        }
                        .doOnNext { e -> execCommands(e, server).subscribe() }
                        .filter { e -> e.message.channelId.asLong() == configuration.getString("discordChannel").toLongOrNull() }
                        .filter { e -> !e.message.content.startsWith("!") }
                        .flatMap { e ->
                            getProperContent(e)
                                    .doOnNext { content ->
                                        server.broadcastMessage(
                                                MessageHandler.getMessage("minecraft.default", e.member.get().username, e.member.get().discriminator, content)
                                        )
                                    }
                        }
                        .subscribe()
            }
            .block()!!

    init {

        server.pluginManager.registerEvent(Event.Type.PLAYER_CHAT, object : PlayerListener() {
            override fun onPlayerChat(event: PlayerChatEvent?) {
                if (event != null) {
                    if (!FakeOnline.instance.um.isAuthenticated(event.player.name) || DataManager.isUserMuted(event.player.name))
                        return

                    if (DataHandler.isCursed(event.message.split(" "))) {
                        event.player.sendMessage(MessageHandler.getMessage("errors.everyone"))
                        return
                    }

                    WebhookHandler
                        . invokeForListener(MessageHandler.getMessage("discord.default", event.message), event.player.name, configuration)
                }
            }
        }, Event.Priority.Normal, this@getBot)

        server.pluginManager.registerEvent(Event.Type.PLAYER_JOIN, object : PlayerListener() {
            override fun onPlayerJoin(event: PlayerJoinEvent?) {
                WebhookHandler.invokeForListener(MessageHandler.getMessage("discord.join", event?.player?.name), event?.player?.name, configuration)
            }
        }, Event.Priority.Normal, this@getBot)

        server.pluginManager.registerEvent(Event.Type.PLAYER_QUIT, object : PlayerListener() {
            override fun onPlayerQuit(event: PlayerQuitEvent?) {
                WebhookHandler.invokeForListener(MessageHandler.getMessage("discord.leave", event?.player?.name), event?.player?.name, configuration)
            }
        }, Event.Priority.Normal, this@getBot)

        server.pluginManager.registerEvent(Event.Type.ENTITY_DEATH, object : EntityListener() {
            override fun onEntityDeath(event: EntityDeathEvent?) {
                if (event?.entity is Player) {
                    val player = event.entity as Player

                    WebhookHandler.invokeForListener(MessageHandler.getMessage("discord.death", player.name), player.name, configuration)
                }
            }
        }, Event.Priority.Normal, this@getBot)
    }

    override fun getClient(): GatewayDiscordClient =
        CLIENT
}