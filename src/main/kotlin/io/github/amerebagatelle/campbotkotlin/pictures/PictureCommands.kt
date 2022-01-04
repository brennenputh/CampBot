package io.github.amerebagatelle.campbotkotlin.pictures

import dev.kord.core.behavior.channel.createMessage
import io.github.amerebagatelle.campbotkotlin.EMBED_GREEN
import io.github.amerebagatelle.campbotkotlin.getErrorEmbed
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
                respond(getErrorEmbed("No attachment in message.\nPlease post a attachment with the command."))
                return@execute
            }

            if(getCategories().contains(args.first)) {
                respond(getErrorEmbed("That category does not exist."))
                return@execute
            }

            val success = upload(args.first, message!!.attachments)

            if (success) {
                respond {
                    title = "Success!  File(s) uploaded."
                    color = EMBED_GREEN
                }
            } else {
                respond(getErrorEmbed("An error occurred while uploading file(s).\nContact bot author to fix the bot."))
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
                color = EMBED_GREEN
            }
        }
    }
    globalCommand("post") {
        description = "Get a file.  Append number to the end for posting more than one (limit 20).  Example: &post staff 1"
        execute(AnyArg("category"), IntegerArg("number").optional(1)) {
            if (args.second > 20) {
                respond(getErrorEmbed("Too many files requested.  Limit is 20."))
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

@Suppress("unused")
fun pictureSlashCommands() = commands("Pictures") {
    globalSlash("categories") {
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
                color = EMBED_GREEN
            }
        }
    }
}