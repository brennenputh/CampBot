package io.github.amerebagatelle.campbotkotlin.utility

import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.create.embed
import io.github.amerebagatelle.campbotkotlin.EMBED_RED
import io.github.amerebagatelle.campbotkotlin.Permissions
import io.github.amerebagatelle.campbotkotlin.getDataDirectory
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.commands.commands
import kotlin.system.exitProcess

@Suppress("unused")
fun ownerUtilityCommands() = commands("Owner", Permissions.BOT_OWNER) {
    globalCommand("shutdown") {
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
    globalCommand("quotesfile") {
        description = "Get the quotes file from the bot."
        execute {
            channel.createMessage {
                embed {
                    title = "The quote file for you..."
                }
                addFile(getDataDirectory().resolve("quotes.json"))
            }
        }
    }
}