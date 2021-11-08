package io.github.amerebagatelle.campbotkotlin.quotes

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import io.github.amerebagatelle.campbotkotlin.info.getInfo
import me.jakejmattson.discordkt.dsl.listeners

lateinit var createQuoteId: Snowflake

suspend fun createQuoteMessageCommands(kord: Kord, guild: Guild) {
    createQuoteId = kord.createGuildMessageCommand(guild.id, "CreateQuote") {}.id
}

@Suppress("unused")
fun quoteMessageCommands() = listeners {
    on<MessageCommandInteractionCreateEvent> {
        val response = interaction.acknowledgePublic()
        if (interaction.invokedCommandId == createQuoteId) {
            for (message in interaction.messages.values) {
                val authorName = getInfo(message.author!!.id).realName
                if (authorName.isEmpty()) {
                    response.followUp {
                        content = "User must set real name in the info command before you can create a quote."
                    }
                }
                val quoteNumber = Quotes.createQuote(authorName, message.content, quotedBy = interaction.user.asUser().asMember(interaction.data.guildId.value!!).username)
                response.followUp {
                    content = String.format("Created quote %s: %s - %s", quoteNumber, authorName, message.content)
                }
            }
        }
    }
}