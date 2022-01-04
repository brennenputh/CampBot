package io.github.amerebagatelle.campbotkotlin.quotes

import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.toReaction
import io.github.amerebagatelle.campbotkotlin.EMBED_GREEN
import io.github.amerebagatelle.campbotkotlin.EMBED_RED
import io.github.amerebagatelle.campbotkotlin.getErrorEmbed
import io.github.amerebagatelle.campbotkotlin.info.getInfo
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.arguments.MessageArg
import me.jakejmattson.discordkt.arguments.QuoteArg
import me.jakejmattson.discordkt.commands.commands
import kotlin.math.floor
import kotlin.random.Random

@Suppress("unused")
fun quotesCommands() = commands("Quotes") {
    globalCommand("createquote", "cq", "cp") {
        description = "Create a quote.  Takes two quote arguments, Content and Author.  Example: &createquote \"content\" \"author\""
        execute(QuoteArg("content"), QuoteArg("author")) {
            message!!.addReaction(Emojis.eyes.toReaction())

            respond(createQuoteWithMessage(args.second, args.first, "${message?.author?.username}#${message?.author?.discriminator}"))
        }
    }
    globalCommand("quote") {
        description = "Get a quote.  Quotes are found by number.  Example: &quote 1"
        execute(IntegerArg("quoteNumber")) {
            respond(getQuoteMessageForNumber(args.first))
        }
    }
    globalCommand("randomquote", "rq") {
        description = "Get a random quote.  Example: &randomquote"
        execute(IntegerArg.optional(1)) {
            if (args.first > 40) {
                respond(getErrorEmbed("Too many quotes requested.  Max is 40."))
                return@execute
            }
            repeat(args.first) {
                respond (getQuoteMessageForNumber(Random.Default.nextInt(quoteTotal()) + 1))
                delay(3000)
            }
        }
    }
    globalCommand("search") {
        description = "Search for a phrase in the quotes file.  Optional second argument to search by author name.  Example: &search \"foo\" false"
        execute(QuoteArg("phrase")) {
            val quotes = search(args.first)
            if (quotes.isNotEmpty()) {
                respondMenu {
                    for (i in quotes.indices step 20) {
                        val subQuotes = quotes.slice(i until quotes.size).take(20)
                        val stringBuilder = StringBuilder()
                        for (quote in subQuotes) {
                            stringBuilder.append("#").append(quote.number).append(": ").append(quote.content)
                                .append(" - ")
                                .append(quote.author).append("\n")
                        }
                        page {
                            title = "Page #${(floor(i.toDouble() / 20) + 1).toInt()}:"
                            description = stringBuilder.toString()
                            color = EMBED_GREEN
                        }
                    }

                    if (quotes.size > 20) {
                        buttons {
                            button("Previous Page", Emojis.arrowLeft) {
                                previousPage()
                            }

                            button("Next Page", Emojis.arrowRight) {
                                nextPage()
                            }
                        }
                    }
                }
            } else {
                respond {
                    title = "No results found."
                    description = "Did not find any matches for your query."
                    color = EMBED_RED
                }
            }
        }
    }
}

@Suppress("unused")
fun quoteSlashCommands() = commands("Quotes") {
    globalSlash("createquote") {
        description = "Create a quote."
        execute(QuoteArg(name = "content"), QuoteArg(name = "author")) {
            respond(embedBuilder = createQuoteWithMessage(args.second, args.first, "${author.username}#${author.discriminator}"), ephemeral = false)
        }
    }
    globalSlash("createquotemessage", "Create Quote") {
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
    globalSlash("quote") {
        description = "Get a quote."
        execute(IntegerArg(name = "quoteNumber")) {
            respond(getQuoteMessageForNumber(args.first))
        }
    }
    globalSlash("randomquote") {
        description = "Get a random quote."
        execute {
            respond(getQuoteMessageForNumber(Random.Default.nextInt(quoteTotal()) + 1))
        }
    }
}