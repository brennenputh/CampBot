package io.github.amerebagatelle.campbotkotlin.pictures

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.followUpEphemeral
import io.github.amerebagatelle.campbotkotlin.EMBED_GREEN
import io.github.amerebagatelle.campbotkotlin.getErrorEmbed
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.AttachmentArg
import me.jakejmattson.discordkt.arguments.ChoiceArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands

@Suppress("unused")
fun pictureCommands() = commands("Pictures") {
    globalCommand("upload") {
        description = "Upload a file to the bot.  Expects an attachment.  Example: &upload staff"
        execute(AnyArg("category"), AttachmentArg("picture")) {
            if(!getCategories().contains(args.first)) {
                respond(getErrorEmbed("That category does not exist."))
                return@execute
            }

            respond(uploadWithMessage(args.first, args.second))
        }
    }
    slash("post") {
        description = "Get a file.  Append number to the end for posting more than one (limit 20).  Example: &post staff 1"
        execute(ChoiceArg("category", "The possible picture categories.", choices = getCategories()), IntegerArg("number", "The number of pictures the bot should post.").optional(1)) {
            if (args.second > 20) {
                respond(getErrorEmbed("Too many files requested.  Limit is 20."))
                return@execute
            }
            if(!getCategories().contains(args.first)) {
                respond(getErrorEmbed("That category does not exist."))
                return@execute
            }
            repeat(args.second) {
                if(interaction != null) interaction?.acknowledgeEphemeral()?.let {
                    it.followUpEphemeral {
                        addFile(randomPicture(args.first))
                    }
                } else {
                    channel.createMessage {
                        addFile(randomPicture(args.first))
                    }
                }
                delay(3000)
            }
        }
    }
}

@Suppress("unused")
fun pictureSlashCommands() = commands("Pictures") {
    slash("categories") {
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