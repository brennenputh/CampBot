package io.github.amerebagatelle.campbotkotlin.commands

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createMessage
import io.github.amerebagatelle.campbotkotlin.features.Pictures
import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.commands.commands
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
        description = "Get a file.  Example: &post staff"
        execute(AnyArg) {
            channel.createMessage {
                addFile(Pictures.randomPicture(args.first).toPath())
            }
        }
    }
}
