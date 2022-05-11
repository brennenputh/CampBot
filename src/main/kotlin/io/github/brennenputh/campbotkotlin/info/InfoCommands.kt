package io.github.brennenputh.campbotkotlin.info

import io.github.brennenputh.campbotkotlin.EMBED_GREEN
import io.github.brennenputh.campbotkotlin.Permissions
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.ChoiceArg
import me.jakejmattson.discordkt.arguments.UserArg
import me.jakejmattson.discordkt.commands.commands

@Suppress("unused")
fun slashInfoCommands() = commands("info") {
    slash("getInfo", "Get Info") {
        description = "Get the info on a user, by ID"
        execute(UserArg("user")) {
            val info = getInfo(args.first.id)
            respondPublic("") {
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
    slash("updateInfo") {
        description = "Update the info the bot has on you."
        execute(ChoiceArg("infoValue", "The info you would like to update.", "realName", "location"), AnyArg("setpoint")) {
            val info = getInfo(author.id)
            info.id = author.id.toString()
            info.username = author.username
            when (args.first) {
                "realName" -> info.realName = args.second
                "location" -> info.location = args.second
            }
            updateInfo(author.id, info)
            respondPublic("") {
                title = "Info updated."
                description = "Your ${args.first} has been updated."
                color = EMBED_GREEN
            }
        }
    }
    slash("executiveUpdateInfo") {
        requiredPermission = Permissions.BOT_OWNER
        description = "Bot-owner only.  Allows updating the info of any user."
        execute(ChoiceArg("infoValue", "The info you would like to update.", "realName", "location"), UserArg("user"), AnyArg("setpoint")) {
            val info = getInfo(args.second.id)
            info.id = args.second.id.toString()
            info.username = args.second.username
            when (args.first) {
                "realName" -> info.realName = args.third
                "location" -> info.location = args.third
            }
            updateInfo(args.second.id, info)
            respondPublic("") {
                title = "Info updated."
                description = "${args.second.username}'s ${args.first} has been updated."
                color = EMBED_GREEN
            }
        }
    }
}