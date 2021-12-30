package io.github.amerebagatelle.campbotkotlin.quotes

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.toReaction
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.arguments.BooleanArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.arguments.QuoteArg
import me.jakejmattson.discordkt.commands.commands
import kotlin.io.path.Path
import kotlin.math.floor
import kotlin.random.Random

@Suppress("unused")
fun quotesCommands() = commands("Quotes") {
    globalCommand("createquote", "cq", "cp") {
        description =
            "Create a quote.  Takes two quote arguments, Content and Author.  Example: &createquote \"content\" \"author\""
        execute(QuoteArg("quote"), QuoteArg("author")) {
            message!!.addReaction(Emojis.eyes.toReaction())

            val quoteNumber = createQuote(args.second, args.first, message?.author?.username + "#" + message?.author?.discriminator)
            respond {
                title = "Created quote #$quoteNumber"
                description = "${args.first} - ${args.second}"
                color = Color(0, 255, 0)
            }

            // On a multiple of 100 quotes, post the quote file
            if (quoteNumber % 100 == 0) {
                channel.createMessage {
                    embed {
                        title = "You have earned the quotes file, being that you are now at $quoteNumber quotes."
                    }
                    addFile(Path("./quotes.json"))
                }
            }
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
                respond(getQuoteMessageForNumber(Random.Default.nextInt(quoteTotal()) + 1))
                delay(3000)
            }
        }
    }
    globalCommand("search") {
        description =
            "Search for a phrase in the quotes file.  Optional second argument to search by author name.  Example: &search \"foo\" false"
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
    slash("screatequote") {
        description =
            "Create a quote."
        execute(QuoteArg(name = "content"), QuoteArg(name = "author")) {
            val quoteNumber = createQuote(args.second, args.first, author.username + "#" + author.discriminator)
            respond {
                title = "Created quote #$quoteNumber"
                description = "${args.first} - ${args.second}"
                color = Color(0, 255, 0)
            }

            // On a multiple of 100 quotes, post the quote file
            if (quoteNumber % 100 == 0) {
                channel.createMessage {
                    embed {
                        title = "You have earned the quotes file, being that you are now at $quoteNumber quotes."
                    }
                    addFile(Path("./quotes.json"))
                }
            }
        }
    }
    slash("squote") {
        description = "Get a quote."
        execute(IntegerArg(name = "quoteNumber")) {
            respond(getQuoteMessageForNumber(args.first))
        }
    }
    slash("srandomquote") {
        description = "Get a random quote."
        execute {
            val quote = findQuote(Random.Default.nextInt(quoteTotal()) + 1)!!
            respond(getQuoteMessage(quote))
        }
    }
}