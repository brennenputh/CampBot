package io.github.brennenputh.campbotkotlin

import dev.kord.common.entity.Snowflake
import io.github.cdimascio.dotenv.dotenv

class Configuration {
    private val dotenv = dotenv {
        directory = getDataDirectory().toAbsolutePath().toString()
        ignoreIfMissing = true
    }

    val botToken: String
        get() {
            return dotenv["BOT_TOKEN"] ?: throw IllegalStateException("BOT_TOKEN is not set")
        }

    val chaosRoleId: Snowflake
        get() {
            return getSnowflake("CHAOS_ROLE_ID") ?: run {
                logger.error("CHAOS_ROLE_ID environment variable not set")
                Snowflake.min
            }
        }

    val chaosChannelId: Snowflake
        get() {
            return getSnowflake("CHAOS_CHANNEL_ID") ?: run {
                logger.error("CHAOS_CHANNEL_ID environment variable not set")
                Snowflake.min
            }
        }

    val prayerRequestsChannelId: Snowflake
        get() {
            return getSnowflake("PRAYER_REQUESTS_CHANNEL_ID") ?: run {
                logger.error("PRAYER_REQUESTS_CHANNEL_ID environment variable not set")
                Snowflake.min
            }
        }

    val generalChannelId: Snowflake
        get() {
            return getSnowflake("GENERAL_CHANNEL_ID") ?: run {
                logger.error("GENERAL_CHANNEL_ID environment variable not set")
                Snowflake.min
            }
        }

    private fun getSnowflake(name: String): Snowflake? = dotenv[name]?.toLong()?.let { Snowflake(it) }
}