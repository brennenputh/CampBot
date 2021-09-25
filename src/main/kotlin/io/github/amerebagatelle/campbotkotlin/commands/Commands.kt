package io.github.amerebagatelle.campbotkotlin.commands

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.create.embed
import io.github.amerebagatelle.campbotkotlin.Permissions
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.api.commands.commands
import kotlin.io.path.Path
import kotlin.system.exitProcess

@Suppress("unused")
fun utilityCommands() = commands("Utility", Permissions.BOT_OWNER) {
    globalCommand("shutdown") {
        description = "Shut down the bot."
        execute {
            respond {
                title = "Shutting down in five seconds..."
                color = Color(255, 0, 0)
            }
            delay(5000)
            message!!.kord.shutdown()
            exitProcess(0)
        }
    }
    globalCommand("quotesfile") {
        description = "Get the quotes file from the bot."
        execute {
            channel.createMessage {
                embed {
                    title = "The quote file for you..."
                }
                addFile(Path("./quotes.json"))
            }
        }
    }
}