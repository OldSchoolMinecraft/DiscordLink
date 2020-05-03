package dev.shog.osmlink

import dev.shog.osmlink.handle.MessageHandler
import discord4j.core.event.domain.message.MessageCreateEvent
import org.bukkit.Server
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import java.util.*

/**
 * Commands for the Discord bot.
 */
val commands = HashMap<String, MessageCreateEvent.(server: Server) -> Mono<*>>().apply {
    put("!online") {
        message.channel
            .flatMap { ch -> ch.createMessage(MessageHandler.getMessage("commands.online", it.onlinePlayers.size.toString())) }
    }

    put("!list") { server ->
        val str = server.onlinePlayers.asSequence()
            .joinToString { "`${it.name}`" }
            .trim()
            .removeSuffix(",")

        message.channel
            .flatMap { ch -> ch.createMessage(
                MessageHandler.getMessage("commands.list",
                server.onlinePlayers.size.toString(),
                str
            )) }
    }
}

/**
 * Find commands in the content and execute them.
 */
fun execCommands(messageCreateEvent: MessageCreateEvent, server: Server): Mono<*> {
    val content = messageCreateEvent.message.content

    return commands
        .toList()
        .toFlux()
        .filter { obj -> content == obj.first }
        .singleOrEmpty()
        .flatMap { command -> command.second.invoke(messageCreateEvent, server) }
}