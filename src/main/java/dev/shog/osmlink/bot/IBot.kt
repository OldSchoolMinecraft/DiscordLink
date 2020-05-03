package dev.shog.osmlink.bot

import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient

/**
 * A bot.
 */
interface IBot {
    fun getClient(): GatewayDiscordClient
}