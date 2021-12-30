package io.github.amerebagatelle.campbotkotlin.quotes

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.embed
import io.github.amerebagatelle.campbotkotlin.info.getInfo
import me.jakejmattson.discordkt.dsl.listeners

lateinit var createQuoteId: Snowflake

suspend fun createQuoteMessageCommands(kord: Kord, guild: Guild) {
    createQuoteId = kord.createGuildMessageCommand(guild.id, "Create Quote") {}.id
}

@Suppress("unused")
fun quoteMessageCommands() = listeners {
    on<MessageCommandInteractionCreateEvent> {
        if (interaction.invokedCommandId == createQuoteId) {
            for (message in interaction.messages.values) {
                val authorName = getInfo(message.author!!.id).realName
                if (authorName.isEmpty()) {
                    interaction.acknowledgeEphemeral().followUpEphemeral {
                        content = "User must set real name in the info command before you can create a quote."
                    }
                    return@on
                }
                val quoteNumber = createQuote(authorName, message.content, quotedBy = interaction.user.asUser().asMember(interaction.data.guildId.value!!).username)
                interaction.acknowledgePublic().followUp {
                    embed {
                        title = "Quote #$quoteNumber"
                        description = "$authorName - ${message.content}"
                    }
                }
            }
        }
    }
}