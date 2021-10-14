package io.github.amerebagatelle.campbotkotlin.quotes

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.rest.builder.message.create.embed
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.toReaction
import me.jakejmattson.discordkt.api.arguments.BooleanArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.arguments.QuoteArg
import me.jakejmattson.discordkt.api.commands.commands
import java.io.File
import kotlin.io.path.Path
import kotlin.math.floor
import kotlin.random.Random

@Suppress("unused")
fun quotesCommands() = commands("Quotes") {
    globalCommand("createquote", "cq", "cp") {
        description =
            "Create a quote.  Takes two quote arguments, Content and Author.  Example: &createquote \"content\" \"author\""
        execute(QuoteArg, QuoteArg) {
            message!!.addReaction(Emojis.eyes.toReaction())

            val quoteNumber = Quotes.createQuote(args.second, args.first, message?.author?.username + "#" + message?.author?.discriminator)
            respond {
                title = "Created quote #$quoteNumber"
                description = String.format("%s - %s", args.first, args.second)
                color = Color(0, 255, 0)
            }
            if (quoteNumber.toInt() == 1000) {
                channel.createMessage {
                    content = "1K QUOTES, LETS GOOOOOOO\n With that out of the way, see #general."
                }
                guild?.getChannelOf<GuildMessageChannel>(Snowflake(757774334478778511))?.createMessage {
                    embed {
                        title = "1000 QUOTES"
                        description = File("./1k.txt").readText()
                    }
                    addFile(Path("./quotes.json"))
                }
            } else if (quoteNumber.toInt() % 100 == 0) {
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
        execute(IntegerArg) {
            val quote = Quotes.findQuote(args.first)
            if (quote != null) {
                respond {
                    title = "Quote #" + quote.number
                    description = String.format("%s - %s", quote.content, quote.author)
                    footer {
                        text = String.format("Quoted by: " + quote.quotedBy)
                    }
                    color = Color(0, 255, 0)
                }
            } else {
                respond {
                    title = "Error"
                    description = "Could not find quote, does it exist?"
                    color = Color(255, 0, 0)
                }
            }
        }
    }
    globalCommand("randomquote", "rq") {
        description = "Get a random quote.  Example: &randomquote"
        execute {
            val quote = Quotes.findQuote(Random.Default.nextInt(Quotes.quoteTotal()) + 1)!!
            respond {
                title = "Quote #" + quote.number
                description = String.format("%s - %s", quote.content, quote.author)
                footer {
                    text = String.format("Quoted by: " + quote.quotedBy)
                }
                color = Color(0, 255, 0)
            }
        }
    }
    globalCommand("search") {
        description =
            "Search for a phrase in the quotes file.  Optional second argument to search by author name.  Example: &search \"foo\" false"
        execute(QuoteArg, BooleanArg.optional(false)) {
            val quotes = Quotes.search(args.first, args.second)
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
                            title = "Page #${floor(i.toDouble() / 20) + 1}:"
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