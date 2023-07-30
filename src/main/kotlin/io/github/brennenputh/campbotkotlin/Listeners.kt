package io.github.brennenputh.campbotkotlin

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.request.KtorRequestException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import me.jakejmattson.discordkt.dsl.listeners
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val urlRegex = Regex("https?://(?:canary\\.)?discord\\.com/channels/(\\d+)/(\\d+)/(\\d+)$")
val quoteInlineRegex = Regex("\\{#(\\d+)}")

var lastPrayerRequestTimestamp: Long = 0

@Suppress("unused")
fun memberJoinListener() = listeners {
    on<MemberJoinEvent> {
        guild.getChannelOf<TextChannel>(config.generalChannelId).createEmbed {
            title = "Welcome ${member.username}!"
            description =
                "Please state your real name so that we know who you are.\nAlso, please state your current position on camp (LIT, JL, CL, SL)."
        }
        guild.invites.filter { invite -> invite.uses > 0 }.collect { it.delete("Invite was used.") }
    }
}

@Suppress("unused")
fun messageListener() = listeners {
    on<MessageCreateEvent> {
        val quoteInlines = quoteInlineRegex.findAll(message.content)
        for (inline in quoteInlines) {
            message.channel.createEmbed {
                getQuoteMessageForNumber(
                    Integer.parseInt(inline.groupValues[1]),
                    kord
                ).invoke(this)
            }
        }
    }

    on<MessageCreateEvent> {
        if (message.channelId != config.prayerRequestsChannelId) return@on

        if (System.nanoTime() - lastPrayerRequestTimestamp > 2 * 6e+10) {
            (message.getChannel() as TextChannel).startPublicThreadWithMessage(
                message.id,
                "${message.getAuthorAsMember()?.displayName} ${
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM dd yyyy"))
                }"
            )
            lastPrayerRequestTimestamp = System.nanoTime()
        }
    }

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
                val error = message.channel.createEmbed {
                    getErrorEmbed(
                        when (e.httpResponse.status.value) {
                            403 -> "No access to linked message."
                            404 -> "Linked message not found."
                            else -> "Could not get message from link."
                        }
                    ).invoke(this)
                }
                delay(5000)
                error.delete()
            }
        }
    }
}
