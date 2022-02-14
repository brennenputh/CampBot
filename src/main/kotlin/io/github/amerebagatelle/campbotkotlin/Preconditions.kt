package io.github.amerebagatelle.campbotkotlin

import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.dsl.precondition

@Suppress("unused")
fun logPrecondition() = precondition {
    logger.info("Command: ${command?.name} User: ${author.username} (${author.id})  Channel: ${channel.data.name.value ?: "DM"} (${channel.id})")
}

@Suppress("unused")
fun messageCommandDeprecation() = precondition {
    if (message?.content?.startsWith("&") == true) {
        if(message?.content?.contains("createquote") == true) {
            message?.channel?.createMessage("The command `&createquote` has been removed. Please use `/createquote` instead.")
            fail()
        }

        val message = message?.channel?.createMessage("This command is deprecated. Please try the slash command instead. (type `/` in the chat bar).  This version of the command only exists for the sake of mobile devices.")
        delay(5000)
        message?.delete()
    }
}