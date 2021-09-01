package io.github.amerebagatelle.campbotkotlin

import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.request.KtorRequestException
import io.github.amerebagatelle.campbotkotlin.features.Quotes
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.api.dsl.bot
import me.jakejmattson.discordkt.api.dsl.listeners

const val test = false

@KordPreview
fun main() {
    if(!test) {
        val token = "Njk2NTE0ODkwMzYxMzM5OTM0.Xop2Cg.ev3ezWxSA5rsrhflUHcD-mYzeho"

        //val commands = Commands()
        //commands.registerCommands()

        bot(token) {
            prefix { "&" }
        }
    } else {
        test()
    }
}

fun test() {
    val quotes = Quotes.search("nothing", false)
    for (quote in quotes) {
        println(quote.author + " " + quote.content + " " + quote.number)
    }
}

val urlRegex = Regex("https?://(?:canary\\.)?discord\\.com/channels/(.*?)/(.*?)/(.*?)$")
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
                val message = grabbedChannel?.getMessage(messageId)
                message?.channel?.createEmbed {
                    title = "Quoted from " + message.author?.username
                    description = message.content
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
    }

}
