package io.github.brennenputh.campbotkotlin

import me.jakejmattson.discordkt.dsl.precondition

@Suppress("unused")
fun logPrecondition() = precondition {
    logger.info("Command: ${command?.name} User: ${author.username} (${author.id})  Channel: ${channel.data.name.value ?: "DM"} (${channel.id})")
}

@Suppress("unused")
fun messageCommandDeprecation() = precondition {
    if (message?.content?.startsWith("&") == true) {
        message?.channel?.createMessage("This version of the command has been removed.  Please use the slash command instead. (type `/` in the chat bar).")
        fail()
    }
}