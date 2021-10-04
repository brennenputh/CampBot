package io.github.amerebagatelle.campbotkotlin.chaos

import dev.kord.core.entity.channel.TextChannel
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.toReaction
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.arguments.QuoteArg
import me.jakejmattson.discordkt.api.commands.commands
import me.jakejmattson.discordkt.api.extensions.addReactions

private val ALPHABET = listOf<DiscordEmoji>(
    Emojis.regionalIndicatorA,
    Emojis.regionalIndicatorB,
    Emojis.regionalIndicatorC,
    Emojis.regionalIndicatorD,
    Emojis.regionalIndicatorE,
    Emojis.regionalIndicatorF,
    Emojis.regionalIndicatorG,
    Emojis.regionalIndicatorH,
    Emojis.regionalIndicatorI,
    Emojis.regionalIndicatorJ,
    Emojis.regionalIndicatorK,
    Emojis.regionalIndicatorL,
    Emojis.regionalIndicatorM,
    Emojis.regionalIndicatorN,
    Emojis.regionalIndicatorO
).map { emoji -> emoji.toReaction() }

@Suppress("unused")
fun pollCommands() = commands("poll") {
    command("poll") {
        description = "Create a poll (inside a thread).  Takes a topic to poll on and the number of possible options."
        execute(QuoteArg, IntegerArg) {
            respond("${author.username} has started a poll: ${args.first}")

            val thread = (channel as TextChannel).startPublicThread("Poll for ${args.first}")
            thread.join()

            val pollingMessage = thread.createMessage("**" + args.first + "?**")

            pollingMessage.addReactions(ALPHABET.take(args.second))

            pollingMessage.pin()
        }
    }
}