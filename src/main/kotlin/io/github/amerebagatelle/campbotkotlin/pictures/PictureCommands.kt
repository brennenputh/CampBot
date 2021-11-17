package io.github.amerebagatelle.campbotkotlin.utility

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createMessage
import io.github.amerebagatelle.campbotkotlin.pictures.Pictures
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands
import java.io.File

@Suppress("unused")
fun pictureCommands() = commands("Pictures") {
    globalCommand("upload") {
        description = "Upload a file to the bot.  Expects an attachment.  Example: &upload staff"
        execute(AnyArg) {
            if (message!!.attachments.isEmpty()) {
                respond {
                    title = "Error"
                    description = "Please attach a file with your message."
                    color = Color(255, 0, 0)
                }
            } else {
                val success = Pictures.upload(args.first, message!!.attachments)

                if (success) {
                    respond {
                        title = "Success!  File(s) uploaded."
                    }
                } else {
                    respond {
                        title = "Failure.  Contact bot owner to fix the bot."
                    }
                }
            }
        }
    }
    globalCommand("categories") {
        description = "Get the list of available categories of files.  Example: &categories"
        execute {
            val dirs = File("pictures/").list()!!
            val stringBuilder = StringBuilder()
            for (dir in dirs) {
                stringBuilder.append("`").append(dir).append("`\n")
            }
            respond {
                title = "Categories"
                description = stringBuilder.toString()
                color = Color(0, 255, 0)
            }
        }
    }
    globalCommand("post") {
        description = "Get a file.  Append number to the end for posting more than one (limit 20).  Example: &post staff 1"
        execute(AnyArg, IntegerArg.optional(1)) {
            if (args.second > 20) {
                respond {
                    title = "Error"
                    description = "Number to post greater than 20, please be more reasonable."
                    color = Color(255, 0, 0)
                }
                return@execute
            }
            repeat(args.second) {
                channel.createMessage {
                    addFile(Pictures.randomPicture(args.first).toPath())
                }
                delay(3000)
            }
        }
    }
}
