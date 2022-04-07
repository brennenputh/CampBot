package io.github.brennenputh.campbotkotlin

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.request.KtorRequestException
import io.github.brennenputh.campbotkotlin.quotes.getQuoteMessageForNumber
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.dsl.listeners
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val urlRegex = Regex("https?://(?:canary\\.)?discord\\.com/channels/(\\d+)/(\\d+)/(\\d+)$")
val quoteInlineRegex = Regex("\\{#(\\d+)}")

val googleRegex = Regex("[Gg]+[Oo]+[Gg]+[Ll]+[Ee]+")

var lastPunishmentThreadTimestamp: Long = 0
var lastPrayerRequestTimestamp: Long = 0

@Suppress("unused")
fun messageListener() = listeners {
    // Inline quotes
    on<MessageCreateEvent> {
        // Check for inlined quotes in the message
        val quoteInlines = quoteInlineRegex.findAll(message.content)
        for (inline in quoteInlines) {
            message.channel.createEmbed { getQuoteMessageForNumber(Integer.parseInt(inline.groupValues[1])).invoke(this) }
        }
    }
    // Auto-create threads in prayer requests
    on<MessageCreateEvent> {
        if (message.channelId != config.prayerRequestsChannelId) return@on

        if (System.nanoTime() - lastPrayerRequestTimestamp > 10 * 6e+10) {
            (message.getChannel() as TextChannel).startPublicThreadWithMessage(
                message.id,
                "${message.getAuthorAsMember()?.displayName} ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM dd yyyy"))}"
            )
            lastPrayerRequestTimestamp = System.nanoTime()
        }
    }
    // Expand discord message links
    on<MessageCreateEvent> {
        val links = urlRegex.findAll(message.content)
        for (link in links) {
            try {
                val serverId = Snowflake(link.groupValues[1])
                val channelId = Snowflake(link.groupValues[2])
                val messageId = Snowflake(link.groupValues[3])

                val server = kord.getGuild(serverId)
                val grabbedChannel = server?.getChannelOf<TextChannel>(channelId)
                val grabbedMessage = grabbedChannel?.getMessage(messageId)
                message.channel.createEmbed {
                    title = "Quoted from " + grabbedMessage?.author?.username
                    description = grabbedMessage?.content
                }
            } catch (e: KtorRequestException) {
                val error = message.channel.createEmbed { getErrorEmbed(when(e.httpResponse.status.value) {
                    403 -> "No access to linked message."
                    404 -> "Linked message not found."
                    else -> "Could not get message from link."
                }).invoke(this)
                }
                delay(5000)
                error.delete()
            }
        }
    }
    // Chaos rules implementation
    on<MessageCreateEvent> {
        if(message.interaction != null) return@on
        if (message.author!!.isBot) return@on
        if (message.channelId != config.chaosChannelId) return@on

        if (message.content.contains("productiv", true) || message.content.contains("maniac", true)) {
            logger.info("Rulebreaker detected")
            message.channel.createEmbed {
                title = "RULES"
                description = "A MESSAGE IN #chaos MUST NOT HAVE THAT WORD IN IT"
                color = EMBED_RED
            }

            val chaosRole = getGuild()?.getRole(config.chaosRoleId)

            if (member?.roles!!.any { role -> role == chaosRole } && System.nanoTime() - lastPunishmentThreadTimestamp > 100 * 6e+10) {
                val thread = (message.getChannel() as TextChannel).startPublicThreadWithMessage(
                    message.id,
                    "Chaos Rulebreak " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM dd yyyy HH mm"))
                )
                thread.join()
                thread.createMessage("A rule has been broken.  This must be taken to the courts.  " + chaosRole?.mention)
                lastPunishmentThreadTimestamp = System.nanoTime()
            }
        }

        if (message.content.contains(googleRegex)) {
            logger.info("Google detected")
            message.channel.createEmbed {
                title = "THE GOOOOOOOOOGLE"
                description = "NOT THE GOOGLE"
                color = EMBED_RED
            }
        }
    }
}
