package io.github.amerebagatelle.campbotkotlin.quotes

import dev.kord.common.Color
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.toReaction
import io.github.amerebagatelle.campbotkotlin.info.getInfo
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.arguments.BooleanArg
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
        execute(QuoteArg("quote"), QuoteArg("author")) {
            message!!.addReaction(Emojis.eyes.toReaction())

            respond(createQuoteWithMessage(args.second, args.first, message?.author?.username + "#" + message?.author?.discriminator))
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
                respond {
                    title = "Number too large."
                    description = "Please make number under 40"
                }
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
        execute(QuoteArg("phrase"), BooleanArg("searchByAuthor").optional(false)) {
            val quotes = search(args.first, args.second)
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
                            color = Color(0, 255, 0)
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
                    title = "Error"
                    description = "Did not find any matches for your query."
                    color = Color(255, 0, 0)
                }
            }
        }
    }
}

@Suppress("unused")
fun quoteSlashCommands() = commands("Quotes") {
    slash("createquote") {
        description = "Create a quote."
        execute(QuoteArg(name = "content"), QuoteArg(name = "author")) {
            respond(createQuoteWithMessage(args.second, args.first, author.username + "#" + author.discriminator), false)
        }
    }
    slash("createquotemessage", "Create Quote") {
        execute(MessageArg) {
            val authorName = args.first.author?.id?.let { getInfo(it) }?.realName ?: run {
                respond {
                    title = "Error"
                    description = "Could not find author."
                    color = Color(255, 0, 0)
                }
                return@execute
            }
            if (authorName.isEmpty()) {
                respond {
                    title = "Error"
                    description = "User must set real name in the info command before you can create a quote."
                    color = Color(255, 0, 0)
                }
                return@execute
            }
            respond(createQuoteWithMessage(args.first.content, authorName, author.username + "#" + author.discriminator), false)
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