package io.github.brennenputh.campbotkotlin

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.rest.builder.message.create.embed
import me.jakejmattson.discordkt.commands.commands

@Suppress("unused")
fun userUtilityCommands() = commands("Utility") {
    slash("quotesfile", description = "Get the quotes file from the bot.") {
        execute {
            interaction?.respondPublic {
                embed {
                    title = "The quote file for you..."
                }
                addFile(getDataDirectory().resolve("quotes.json"))
            }
        }
    }
}