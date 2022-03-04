package io.github.amerebagatelle.campbotkotlin

import dev.kord.common.annotation.KordPreview
import io.github.amerebagatelle.campbotkotlin.pictures.loadPictureCache
import me.jakejmattson.discordkt.dsl.bot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

val config = Configuration()

val logger: Logger = LoggerFactory.getLogger("campbot")

@KordPreview
fun main() {
    bot(config.botToken) {
        prefix { "&" }
        configure {
            commandReaction = null
            theme = java.awt.Color(0, 255, 0)
            permissions = Permissions
        }
        presence {
            watching("ya'll")
        }
        onStart {
            loadPictureCache()

            logger.info("Bot started.")
        }
        onException {
            logger.error("Exception caught\n ${exception.stackTraceToString()}")
        }
    }
}

fun getDataDirectory(): Path = if(System.getenv("dev") != "true") File("/data").toPath() else File(".").toPath()
