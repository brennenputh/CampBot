package io.github.amerebagatelle.campbotkotlin.info

import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.QuoteArg
import me.jakejmattson.discordkt.arguments.UserArg
import me.jakejmattson.discordkt.commands.commands

fun infoCommands() = commands("info") {
    command("getInfo") {
        description = "Get the info on a user, by ID"
        execute(UserArg) {
            val info = getInfo(args.first.id)
            respond {
                title = "Info for ${args.first.username}"
                description = """
                    ID: ${info.id}
                    Username: ${info.username}
                    Real name: ${info.realName}
                    Location: ${info.location}
                """.trimIndent()
            }
        }
    }
    command("updateInfo") {
        description = "Update the info the bot has on you."
        execute(AnyArg, QuoteArg) {
            val info = getInfo(author.id)
            info.id = author.id.asString
            info.username = author.username
            when (args.first) {
                "realName" -> info.realName = args.second
                "location" -> info.location = args.second
            }
            updateInfo(author.id, info)
            respond {
                title = "Info updated"
                description = "Your ${args.first} has been updated."
            }
        }
    }

}