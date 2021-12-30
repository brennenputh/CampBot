package io.github.amerebagatelle.campbotkotlin.pictures

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createMessage
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands

@Suppress("unused")
fun pictureCommands() = commands("Pictures") {
    globalCommand("upload") {
        description = "Upload a file to the bot.  Expects an attachment.  Example: &upload staff"
        execute(AnyArg("category")) {
            if (message!!.attachments.isEmpty()) {
                respond {
                    title = "Error"
                    description = "Please attach a file with your message."
                    color = Color(255, 0, 0)
                }
                return@execute
            }

            if(getCategories().contains(args.first)) {
                respond {
                    title = "Error"
                    description = "That category does not exist.  Run &categories to see all categories."
                    color = Color(255, 0, 0)
                }
                return@execute
            }

            val success = upload(args.first, message!!.attachments)

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
    globalCommand("categories") {
        description = "Get the list of available categories of files.  Example: &categories"
        execute {
            val dirs = getCategories()
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
        execute(AnyArg("category"), IntegerArg("number").optional(1)) {
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
                    addFile(randomPicture(args.first).toPath())
                }
                delay(3000)
            }
        }
    }
}
