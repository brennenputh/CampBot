package io.github.amerebagatelle.campbotkotlin

import dev.kord.common.entity.Snowflake
import me.jakejmattson.discordkt.dsl.PermissionSet
import me.jakejmattson.discordkt.dsl.permission

object Permissions : PermissionSet {
    val BOT_OWNER = permission("Bot Owner") { users(Snowflake(643577385970827266)) }
    val GUILD_OWNER = permission("Guild Owner") { users(guild!!.ownerId) }
    val EVERYONE = permission("Everyone") { roles(guild!!.everyoneRole.id) }

    override val commandDefault = EVERYONE
    override val hierarchy = listOf(EVERYONE, GUILD_OWNER, BOT_OWNER)
}