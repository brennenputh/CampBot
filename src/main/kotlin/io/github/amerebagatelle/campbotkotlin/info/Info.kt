package io.github.amerebagatelle.campbotkotlin.info

import com.beust.klaxon.Klaxon
import dev.kord.common.entity.Snowflake
import java.io.FileReader
import java.io.FileWriter

data class UserInfo(
    var id: String,
    var username: String,
    var realName: String,
    var location: String
)

val klaxon = Klaxon()

fun getInfo(userId: Snowflake): UserInfo {
    val infoList = klaxon.parseArray<UserInfo>(FileReader("userInfo.json"))
    return infoList?.firstOrNull { it.id == userId.asString } ?: UserInfo(userId.asString, "", "", "")
}

fun updateInfo(userId: Snowflake, info: UserInfo) {
    val infoList = klaxon.parseArray<UserInfo>(FileReader("userInfo.json"))!!.toMutableList()

    val entry = infoList.firstOrNull { it.id == userId.asString }?.let {
        it.username = info.username
        it.id = info.id
        it.location = info.location
        it.realName = info.realName
    }
    if (entry == null) infoList.add(info)

    FileWriter("userInfo.json").use {
        it.write(klaxon.toJsonString(infoList))
        it.close()
    }
}