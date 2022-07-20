@file:OptIn(ExperimentalSerializationApi::class)

package io.github.brennenputh.campbotkotlin

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.ChoiceArg
import me.jakejmattson.discordkt.arguments.UserArg
import me.jakejmattson.discordkt.commands.commands
import java.io.File

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
        requiredPermissions = Permissions(Permission.Administrator)
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

@Serializable
data class UserInfo(
    var id: String,
    var username: String,
    var realName: String,
    var location: String
)

val infoFile: File = getDataDirectory().resolve("userInfo.json").toFile()

fun getInfo(userId: Snowflake): UserInfo {
    return Json.decodeFromStream<List<UserInfo>>(infoFile.inputStream()).firstOrNull { it.id == userId.toString() } ?: UserInfo(userId.toString(), "", "", "")
}

private fun updateInfo(userId: Snowflake, info: UserInfo) {
    val infoList = Json.decodeFromStream<List<UserInfo>>(infoFile.inputStream()).toMutableList()

    val entry = infoList.firstOrNull { it.id == userId.toString() }?.let {
        it.username = info.username
        it.id = info.id
        it.location = info.location
        it.realName = info.realName
    }
    if (entry == null) infoList.add(info)

    Json.encodeToStream(infoList.toList(), infoFile.outputStream())
}