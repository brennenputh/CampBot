package io.github.amerebagatelle.campbotkotlin.utility

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.create.embed
import io.github.amerebagatelle.campbotkotlin.Permissions
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.arguments.MessageArg
import me.jakejmattson.discordkt.arguments.QuoteArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.jumpLink
import kotlin.io.path.Path
import kotlin.system.exitProcess

@Suppress("unused")
fun ownerUtilityCommands() = commands("Owner", Permissions.BOT_OWNER) {
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

@Suppress("unused")
fun userUtilityCommands() = commands("Utility") {
    globalCommand("reply") {
        description = "Reply to a specific message, even if it's in another channel."
        execute(MessageArg(name = "replyTo"), QuoteArg(name = "content")) {
            message?.delete()
            channel.createMessage {
                content = "${args.first.author?.mention} ${getMember()?.displayName} replies: ${args.second}"
                embed {
                    title = "Reply To: ${args.first.jumpLink()}"
                    description = "${args.first.content}"
                }
            }
        }
    }
    globalCommand("messageArgTest") {
        description = "Test message arg"
        execute(MessageArg) {
            println(args.first.content)
        }
    }
}