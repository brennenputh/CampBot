package io.github.amerebagatelle.campbotkotlin.info

import com.beust.klaxon.Klaxon
import dev.kord.common.entity.Snowflake
import io.github.amerebagatelle.campbotkotlin.getDataDirectory
import java.io.File
import java.io.FileReader
import java.io.FileWriter

data class UserInfo(
    var id: String,
    var username: String,
    var realName: String,
    var location: String
)

val klaxon = Klaxon()
val infoFile: File = getDataDirectory().resolve("userInfo.json").toFile()

fun getInfo(userId: Snowflake): UserInfo {
    val infoList = klaxon.parseArray<UserInfo>(FileReader(infoFile))
    return infoList?.firstOrNull { it.id == userId.toString() } ?: UserInfo(userId.toString(), "", "", "")
}

fun updateInfo(userId: Snowflake, info: UserInfo) {
    val infoList = klaxon.parseArray<UserInfo>(FileReader(infoFile))!!.toMutableList()

    val entry = infoList.firstOrNull { it.id == userId.toString() }?.let {
        it.username = info.username
        it.id = info.id
        it.location = info.location
        it.realName = info.realName
    }
    if (entry == null) infoList.add(info)

    FileWriter(infoFile).use {
        it.write(klaxon.toJsonString(infoList))
        it.close()
    }
}