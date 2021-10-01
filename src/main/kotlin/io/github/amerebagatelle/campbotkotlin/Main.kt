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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

lateinit var chaosRoleId: Snowflake
lateinit var chaosChannelId: Snowflake

@KordPreview
fun main() {
    val dotenv = dotenv()
    val token = dotenv["TOKEN"]
    chaosRoleId = Snowflake(dotenv["CHAOS_ROLE_ID"].toLong())
    chaosChannelId = Snowflake(dotenv["CHAOS_CHANNEL_ID"].toLong())

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
    println(String.format("Command: %s  User: %s (%s)  Channel: %s (%s)", command?.name, author.username, author.id.asString, channel.data.name.value ?: "DM", channel.id.asString))
}

val urlRegex = Regex("https?://(?:canary\\.)?discord\\.com/channels/(\\d+)/(\\d+)/(\\d+)$")
val quoteInlineRegex = Regex("\\{#(\\d+)}")

@Suppress("unused")
fun messageListener() = listeners {
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
                    title = "Error"
                    description = "Link not from this server."
                }
                delay(5000)
                error.delete()
            }
        }

        val quoteInlines = quoteInlineRegex.findAll(message.content)
        for (inline in quoteInlines) {
            val quote = Quotes.findQuote(Integer.parseInt(inline.groupValues[1]))
            if(quote != null) {
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

        if (message.channelId == chaosChannelId) {
            if (message.content.contains("productiv", true) || message.content.contains("maniac", true)) {
                message.channel.createEmbed {
                    title = "RULES"
                    description = "A MESSAGE IN #chaos MUST NOT HAVE THAT WORD IN IT"
                    color = Color(255, 0, 0)
                }

                val chaosRole = getGuild()?.getRole(chaosRoleId)

                if (member?.roles!!.any { role -> role == chaosRole }) {
                    val thread = (message.getChannel() as TextChannel).startPublicThreadWithMessage(
                        message.id,
                        "Chaos Rulebreak " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).slice(0..18)
                    )
                    thread.join()
                    thread.createMessage("A rule has been broken.  This must be taken to the courts.  " + chaosRole?.mention)
                }
            }

            if (message.content.contains("google", true)) {
                message.channel.createEmbed {
                    title = "THE GOOOOOOOOOGLE"
                    description = "NOT THE GOOGLE"
                    color = Color(255, 0, 0)
                }
            }
        }
    }

}
