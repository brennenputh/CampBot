package io.github.brennenputh.campbotkotlin.utility

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.rest.builder.message.create.embed
import io.github.brennenputh.campbotkotlin.getDataDirectory
import me.jakejmattson.discordkt.commands.commands

@Suppress("unused")
fun userUtilityCommands() = commands("Utility") {
    slash("quotesfile") {
        description = "Get the quotes file from the bot."
        execute {
            interaction?.respondEphemeral {
                embed {
                    title = "The quote file for you..."
                }
                addFile(getDataDirectory().resolve("quotes.json"))
            }
        }
    }
}