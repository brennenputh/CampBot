package io.github.amerebagatelle.campbotkotlin

import me.jakejmattson.discordkt.dsl.precondition

@Suppress("unused")
fun logPrecondition() = precondition {
    logger.info("Command: ${command?.name} User: ${author.username} (${author.id})  Channel: ${channel.data.name.value ?: "DM"} (${channel.id})")
}
