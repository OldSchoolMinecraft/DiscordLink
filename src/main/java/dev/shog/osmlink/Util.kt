package dev.shog.osmlink

import dev.shog.osmlink.handle.MessageHandler
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

/**
 * Format [str] with [args]
 */
fun formatTextArray(str: String, args: Collection<String?>): String {
    var newString = str

    args.forEachIndexed { i, arg ->
        if (newString.contains("{$i}"))
            newString = newString.replace("{$i}", arg ?: "null")
    }

    return newString
}

/**
 * Replace @'s to user name
 */
fun getProperContent(e: MessageCreateEvent): Mono<String> = e.message.content.toMono()
    .flatMap { cnt ->
        e.message.userMentions
            .collectList()
            .map { list ->
                var repl = cnt

                for (en in list) {
                    repl = repl.replace("<@!${en.id.asLong()}>", MessageHandler.getMessage("mentions.user", en.username, en.discriminator))
                }

                repl
            }
    }
    .flatMap { cnt ->
        e.message.roleMentions
            .collectList()
            .map { list ->
                var repl = cnt

                for (en in list) {
                    repl = repl.replace(en.mention, MessageHandler.getMessage("mentions.channel", en.name))
                }

                repl
            }
    }