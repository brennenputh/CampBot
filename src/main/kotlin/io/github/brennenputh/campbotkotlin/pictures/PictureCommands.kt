package io.github.brennenputh.campbotkotlin.pictures

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.response.followUp
import io.github.brennenputh.campbotkotlin.getErrorEmbed
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.arguments.AttachmentArg
import me.jakejmattson.discordkt.arguments.ChoiceArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands

@Suppress("unused")
fun pictureCommands() = commands("Pictures") {
    slash("upload") {
        description = "Upload a file to the bot.  Expects an attachment.  Example: &upload staff"
        execute(ChoiceArg("category", "The category the picture should go in.", choices = getCategories()), AttachmentArg("picture")) {
            if(!getCategories().contains(args.first)) {
                respond(getErrorEmbed("That category does not exist."))
                return@execute
            }

            respond(false, uploadWithMessage(args.first, args.second))
        }
    }
    slash("post") {
        description = "Get a file.  Append number to the end for posting more than one (limit 20).  Example: &post staff 1"
        execute(ChoiceArg("category", "The category the picture should go in.", choices = getCategories()), IntegerArg("number", "The number of pictures the bot should post.").optional(1)) {
            if (args.second > 20) {
                respond(getErrorEmbed("Too many files requested.  Limit is 20."))
                return@execute
            }
            if(!getCategories().contains(args.first)) {
                respond(getErrorEmbed("That category does not exist."))
                return@execute
            }

            respond("Posting pictures...", false)
            repeat(args.second) {
                delay(3000)
                val pic = randomPicture(args.first)
                val response = channel.createMessage {
                    if(pic.url != null) {
                        content = pic.url
                    } else {
                        addFile(pic.path)
                    }
                }
                if(pic.url == null) addToPictureCache(Picture(pic.path, response.attachments.first().url))
            }
        }
    }
}