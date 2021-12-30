package io.github.amerebagatelle.campbotkotlin

import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.any
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.request.KtorRequestException
import io.github.amerebagatelle.campbotkotlin.quotes.createQuoteMessageCommands
import io.github.amerebagatelle.campbotkotlin.quotes.getQuoteMessageForNumber
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvException
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import me.jakejmattson.discordkt.dsl.bot
import me.jakejmattson.discordkt.dsl.listeners
import me.jakejmattson.discordkt.dsl.precondition
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

lateinit var chaosRoleId: Snowflake
lateinit var chaosChannelId: Snowflake
lateinit var prayerRequestsChannelId: Snowflake

@KordPreview
fun main() {
    val dotenv: Dotenv
    try {
        dotenv = dotenv()
    } catch (e: DotenvException) {
        println("Could not load .env file. Please make sure it exists and is formatted correctly.")
        return
    }
    val token = dotenv["TOKEN"] ?: throw IllegalStateException("TOKEN not found in .env file")
    chaosRoleId = dotenv["CHAOS_ROLE_ID"]?.toLong()?.let { Snowflake(it) } ?: run {
        println("CHAOS_ROLE_ID not found in .env file")
        Snowflake.min
    }
    chaosChannelId = dotenv["CHAOS_CHANNEL_ID"]?.toLong()?.let { Snowflake(it) } ?: run {
        println("CHAOS_CHANNEL_ID not found in .env file")
        Snowflake.min
    }
    prayerRequestsChannelId = dotenv["PRAYER_REQUESTS_CHANNEL_ID"]?.toLong()?.let { Snowflake(it) } ?: run {
        println("PRAYER_REQUESTS_CHANNEL_ID not found in .env file")
        Snowflake.min
    }

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
        onStart {
            createQuoteMessageCommands(kord, kord.guilds.first())
        }
        onException {
            exception.printStackTrace()
        }
    }
}

@Suppress("unused")
fun logPrecondition() = precondition {
    val file = File("log/${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM dd yyyy"))}.txt")
    @Suppress("BlockingMethodInNonBlockingContext")
    file.createNewFile()
    file.appendText("Command: ${command?.name} User: ${author.username} (${author.id})  Channel: ${channel.data.name.value ?: "DM"} (${channel.id})  Timestamp: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"))}\n")
}

val urlRegex = Regex("https?://(?:canary\\.)?discord\\.com/channels/(\\d+)/(\\d+)/(\\d+)$")
val quoteInlineRegex = Regex("\\{#(\\d+)}")

val googleRegex = Regex("[Gg]+[Oo]+[Gg]+[Ll]+[Ee]+")

var lastPunishmentThreadTimestamp: Long = 0
var lastPrayerRequestTimestamp: Long = 0

@Suppress("unused")
fun messageListener() = listeners {
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
                val error = message.channel.createEmbed {
                    title = "Error"
                    description = "Link not from this server."
                }
                delay(5000)
                error.delete()
            }
        }
    }
    // Inline quotes
    on<MessageCreateEvent> {
        // Check for inlined quotes in the message
        val quoteInlines = quoteInlineRegex.findAll(message.content)
        for (inline in quoteInlines) {
            message.channel.createMessage {
                getQuoteMessageForNumber(Integer.parseInt(inline.groupValues[1]))
            }
        }
    }
    // Auto-create threads in prayer requests
    on<MessageCreateEvent> {
        if (message.channelId != prayerRequestsChannelId) return@on

        if (System.nanoTime() - lastPrayerRequestTimestamp > 10 * 6e+10) {
            (message.getChannel() as TextChannel).startPublicThreadWithMessage(
                message.id,
                "${message.getAuthorAsMember()?.displayName} ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM dd yyyy"))}"
            )
            lastPrayerRequestTimestamp = System.nanoTime()
        }
    }
    // Chaos rules implementation
    on<MessageCreateEvent> {
        if (message.channelId != chaosChannelId) return@on

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
}
