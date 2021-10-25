package io.github.amerebagatelle.campbotkotlin

import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.any
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.request.KtorRequestException
import io.github.amerebagatelle.campbotkotlin.quotes.Quotes
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.api.dsl.bot
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.dsl.precondition
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

lateinit var chaosRoleId: Snowflake
lateinit var chaosChannelId: Snowflake
lateinit var prayerRequestsChannelId: Snowflake

@KordPreview
fun main() {
    val dotenv = dotenv()
    val token = dotenv["TOKEN"]
    chaosRoleId = Snowflake(dotenv["CHAOS_ROLE_ID"].toLong())
    chaosChannelId = Snowflake(dotenv["CHAOS_CHANNEL_ID"].toLong())
    prayerRequestsChannelId = Snowflake(dotenv["PRAYER_REQUESTS_CHANNEL_ID"].toLong())

    bot(token) {
        prefix { "&" }
        configure {
            commandReaction = null
            theme = java.awt.Color(0, 255, 0)
            permissions(commandDefault = Permissions.EVERYONE)
        }
        presence {
            watching("for your command")
        }
    }
}

@Suppress("unused")
fun logPrecondition() = precondition {
    val file = File("log/${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM dd yyyy"))}.txt")
    @Suppress("BlockingMethodInNonBlockingContext")
    file.createNewFile()
    file.appendText(String.format("Command: %s  User: %s (%s)  Channel: %s (%s)  Timestamp: %s\n", command?.name, author.username, author.id.asString, channel.data.name.value ?: "DM", channel.id.asString, LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"))))
}

val urlRegex = Regex("https?://(?:canary\\.)?discord\\.com/channels/(\\d+)/(\\d+)/(\\d+)$")
val quoteInlineRegex = Regex("\\{#(\\d+)}")

val googleRegex = Regex("[Gg]+[Oo]+[Gg]+[Ll]+[Ee]+")

var lastPunishmentThreadTimestamp: Long = 0
var lastPrayerRequestTimestamp: Long = 0

@Suppress("unused")
fun messageListener() = listeners {
    on<MessageCreateEvent> {
        // Check if there are discord links present in the message
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
                    title = "Error"
                    description = "Link not from this server."
                }
                delay(5000)
                error.delete()
            }
        }

        // Check for inlined quotes in the message
        val quoteInlines = quoteInlineRegex.findAll(message.content)
        for (inline in quoteInlines) {
            val quote = Quotes.findQuote(Integer.parseInt(inline.groupValues[1]))
            if (quote != null) {
                message.channel.createEmbed {
                    title = "Quote #" + quote.number
                    description = String.format("%s - %s", quote.content, quote.author)
                    color = Color(0, 255, 0)
                }
            } else {
                message.channel.createEmbed {
                    title = "Error"
                    description = "Could not find quote, does it exist?"
                    color = Color(255, 0, 0)
                }
            }
        }

        // Check for chaos rules broken (in chaos only)
        if (message.channelId == chaosChannelId) {
            if (message.content.contains("productiv", true) || message.content.contains("maniac", true)) {
                message.channel.createEmbed {
                    title = "RULES"
                    description = "A MESSAGE IN #chaos MUST NOT HAVE THAT WORD IN IT"
                    color = Color(255, 0, 0)
                }

                val chaosRole = getGuild()?.getRole(chaosRoleId)

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
                message.channel.createEmbed {
                    title = "THE GOOOOOOOOOGLE"
                    description = "NOT THE GOOGLE"
                    color = Color(255, 0, 0)
                }
            }
        }

        // Check for prayer request
        if (message.channelId == prayerRequestsChannelId) {
            if (System.nanoTime() - lastPrayerRequestTimestamp > 60 * 6e+10) {
                (message.getChannel() as TextChannel).startPublicThreadWithMessage(
                    message.id,
                    "${message.getAuthorAsMember()?.displayName} ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM dd yyyy"))}"
                )
                lastPrayerRequestTimestamp = System.nanoTime()
            }
        }
    }
}
