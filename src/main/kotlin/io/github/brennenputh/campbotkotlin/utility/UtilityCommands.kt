package io.github.brennenputh.campbotkotlin.utility

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.rest.builder.message.create.embed
import io.github.brennenputh.campbotkotlin.EMBED_RED
import io.github.brennenputh.campbotkotlin.Permissions
import io.github.brennenputh.campbotkotlin.getDataDirectory
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.commands.commands
import kotlin.system.exitProcess

@Suppress("unused")
fun ownerUtilityCommands() = commands("Owner", Permissions.BOT_OWNER) {
    slash("shutdown") {
        description = "Shut down the bot."
        execute {
            respond {
                title = "Shutting down in five seconds..."
                color = EMBED_RED
            }
            delay(5000)
            exitProcess(0)
        }
    }
}

@Suppress("unused")
fun userUtilityCommands() = commands("Utility") {
    slash("quotesfile") {
        description = "Get the quotes file from the bot."
        execute {
            interaction?.respondEphemeral {
                embed {
                    title = "The quote file for you..."
                }
                addFile(getDataDirectory().resolve("quotes.json"))
            }
        }
    }
}