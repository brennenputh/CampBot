package io.github.brennenputh.campbotkotlin

import dev.kord.common.annotation.KordPreview
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import kotlinx.serialization.ExperimentalSerializationApi
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

            logger.info("Bot started.")
        }
        onException {
            logger.error("Exception caught:\n ${exception.stackTraceToString()}")
        }
    }
}

fun getDataDirectory(): Path = if (System.getenv("dev") != "true") File("/data").toPath() else File(".").toPath()
