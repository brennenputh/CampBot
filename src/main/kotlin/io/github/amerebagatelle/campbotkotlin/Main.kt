package io.github.amerebagatelle.campbotkotlin

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import me.jakejmattson.discordkt.dsl.bot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

lateinit var chaosRoleId: Snowflake
lateinit var chaosChannelId: Snowflake
lateinit var prayerRequestsChannelId: Snowflake

val logger: Logger = LoggerFactory.getLogger("campbot")

@KordPreview
fun main() {
    val dotenv: Dotenv = dotenv {
        directory = getDataDirectory().toAbsolutePath().toString()
        ignoreIfMissing = true
    }

    val token = dotenv["TOKEN"] ?: throw IllegalStateException("TOKEN environment variable not set")

    chaosRoleId = dotenv["CHAOS_ROLE_ID"]?.toLong()?.let { Snowflake(it) } ?: run {
        logger.error("CHAOS_ROLE_ID environment variable not set")
        Snowflake.min
    }
    chaosChannelId = dotenv["CHAOS_CHANNEL_ID"]?.toLong()?.let { Snowflake(it) } ?: run {
        logger.error("CHAOS_CHANNEL_ID environment variable not set")
        Snowflake.min
    }
    prayerRequestsChannelId = dotenv["PRAYER_REQUESTS_CHANNEL_ID"]?.toLong()?.let { Snowflake(it) } ?: run {
        logger.error("PRAYER_REQUESTS_CHANNEL_ID environment variable not set")
        Snowflake.min
    }

    bot(token) {
        prefix { "&" }
        configure {
            commandReaction = null
            theme = java.awt.Color(0, 255, 0)
            permissions = Permissions
        }
        presence {
            watching("for your command")
        }
        onStart {
            logger.info("Bot started.")

            kord.guilds.first().commands.collect {
                if(it.name.startsWith("s")) {
                    it.delete()
                }
            }
        }
        onException {
            logger.error("Exception caught\n ${exception.stackTraceToString()}")
        }
    }
}

fun getDataDirectory(): Path = if(System.getenv("dev") != "true") File("/data").toPath() else File(".").toPath()