@file:OptIn(ExperimentalSerializationApi::class)

package io.github.brennenputh.campbotkotlin.info

import com.beust.klaxon.Klaxon
import dev.kord.common.entity.Snowflake
import io.github.brennenputh.campbotkotlin.getDataDirectory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

@Serializable
data class UserInfo(
    var id: String,
    var username: String,
    var realName: String,
    var location: String
)

val klaxon = Klaxon()
val infoFile: File = getDataDirectory().resolve("userInfo.json").toFile()

fun getInfo(userId: Snowflake): UserInfo {
    return Json.decodeFromStream<List<UserInfo>>(infoFile.inputStream()).firstOrNull { it.id == userId.toString() } ?: UserInfo(userId.toString(), "", "", "")
}

fun updateInfo(userId: Snowflake, info: UserInfo) {
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