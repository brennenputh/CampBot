package io.github.amerebagatelle.campbotkotlin.quotes

import io.github.amerebagatelle.campbotkotlin.getErrorEmbed
import io.github.amerebagatelle.campbotkotlin.info.getInfo
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.arguments.MessageArg
import me.jakejmattson.discordkt.arguments.QuoteArg
import me.jakejmattson.discordkt.commands.commands
import kotlin.random.Random

@Suppress("unused")
fun quoteSlashCommands() = commands("Quotes") {
    slash("createquote") {
        description = "Create a quote."
        execute(QuoteArg(name = "content"), QuoteArg(name = "author")) {
            respond(embedBuilder = createQuoteWithMessage(args.second, args.first, "${author.username}#${author.discriminator}"), ephemeral = false)
        }
    }
    slash("createquotemessage", "Create Quote") {
        description = "Create a quote with a message."
        execute(MessageArg) {
            val authorName = args.first.author?.id?.let { getInfo(it) }?.realName ?: run {
                respond(getErrorEmbed("Could not find author.  Is this a bot or webhook?"))
                return@execute
            }
            if (authorName.isEmpty()) {
                respond(getErrorEmbed("No recorded name for this user.\nAuthor must run `&updateInfo realName (name)` first."))
                return@execute
            }
            respond(embedBuilder = createQuoteWithMessage(args.first.content, authorName, "${author.username}#${author.discriminator}"), ephemeral = false)
        }
    }
    slash("quote") {
        description = "Get a quote."
        execute(IntegerArg(name = "quoteNumber")) {
            respond(getQuoteMessageForNumber(args.first))
        }
    }
    slash("randomquote") {
        description = "Get a random quote."
        execute {
            respond(getQuoteMessageForNumber(Random.Default.nextInt(quoteTotal()) + 1))
        }
    }
}