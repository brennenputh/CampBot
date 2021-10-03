package io.github.amerebagatelle.campbotkotlin.chaos

import dev.kord.core.entity.channel.TextChannel
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.toReaction
import me.jakejmattson.discordkt.api.arguments.QuoteArg
import me.jakejmattson.discordkt.api.commands.commands

@Suppress("unused")
fun pollCommands() = commands("poll") {
    command("poll") {
        description = "Create a poll (inside a thread).  Takes a topic to poll on."
        execute(QuoteArg) {
            respond("${author.username} has started a poll: ${args.first}")

            val thread = (channel as TextChannel).startPublicThread("Poll for ${args.first}")
            thread.join()

            val pollingMessage = thread.createMessage("**" + args.first + "?**")
            pollingMessage.addReaction(Emojis.thumbsup.toReaction())
            pollingMessage.addReaction(Emojis.thumbsdown.toReaction())
            pollingMessage.pin()
        }
    }
}