package io.github.brennenputh.campbotkotlin

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.jakejmattson.discordkt.dsl.bot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

val config = Configuration()

val logger: Logger = LoggerFactory.getLogger("campbot")

@OptIn(PrivilegedIntent::class, ExperimentalSerializationApi::class)
@KordPreview
fun main() {
    bot(config.botToken) {
        prefix { "&" }
        configure {
            commandReaction = null
            dualRegistry = false
            theme = java.awt.Color(0, 255, 0)
            intents = Intents.all
        }
        presence {
            watching("ya'll")
        }
        onStart {
            loadPictureCache()

            // Fix bad data choices
            val quotes = Json.decodeFromStream<List<OldQuote>>(quoteFile.inputStream())
            val members = kord.getGuild(Snowflake(757774334478778508))?.members?.toList() ?: throw RuntimeException("Couldn't get members????")
            val mutableQuotes = quotes.toMutableList().map { oldQuote ->
                if (oldQuote.quotedBy != "Unknown") {
                    val memberId = try {
                        members.first { it.username == oldQuote.quotedBy.split("#")[0] }.id
                    } catch (e: Exception) {
                        return@map Quote(oldQuote.number, oldQuote.content, oldQuote.author, Snowflake.min)
                    }
                    return@map Quote(oldQuote.number, oldQuote.content, oldQuote.author, memberId)
                }
                return@map Quote(oldQuote.number, oldQuote.content, oldQuote.author, Snowflake.min)
            }
            Json.encodeToStream(mutableQuotes.toList(), quoteFile.outputStream())

            logger.info("Bot started.")
        }
        onException {
            logger.error("Exception caught:\n ${exception.stackTraceToString()}")
        }
    }
}

fun getDataDirectory(): Path = if(System.getenv("dev") != "true") File("/data").toPath() else File(".").toPath()
