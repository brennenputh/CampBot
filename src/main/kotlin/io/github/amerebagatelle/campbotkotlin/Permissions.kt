package io.github.amerebagatelle.campbotkotlin

import dev.kord.common.entity.Permission
import me.jakejmattson.discordkt.api.dsl.PermissionContext
import me.jakejmattson.discordkt.api.dsl.PermissionSet

enum class Permissions : PermissionSet {
    BOT_OWNER {
        override suspend fun hasPermission(context: PermissionContext) = context.user.id.value == 643577385970827266
    },
    GUILD_OWNER {
        override suspend fun hasPermission(context: PermissionContext) = context.getMember()?.isOwner() ?: false
    },
    ADMIN {
        override suspend fun hasPermission(context: PermissionContext) =
            context.getMember()?.getPermissions()?.contains(Permission.Administrator) ?: false
    },
    EVERYONE {
        override suspend fun hasPermission(context: PermissionContext) = true
    }
}