package io.github.amerebagatelle.campbotkotlin.commands

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.kordLogger
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import io.github.amerebagatelle.campbotkotlin.Permissions
import io.github.amerebagatelle.campbotkotlin.features.Pictures
import io.github.amerebagatelle.campbotkotlin.features.Quotes
import kotlinx.coroutines.delay
import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.arguments.BooleanArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.arguments.QuoteArg
import me.jakejmattson.discordkt.api.commands.commands
import java.io.File
import java.util.*
import kotlin.io.path.Path
import kotlin.system.exitProcess

val random = Random()

@Suppress("unused")
fun pictureCommands() = commands("Pictures") {
    command("upload") {
        description = "Upload a picture to the bot.  Expects an attachment.  Example: &upload staff"
        execute(AnyArg) {
            if(message.attachments.isEmpty()) {
                respond {
                    title = "Error"
                    description = "Please attach a picture with your message."
                    color = Color(255, 0, 0)
                }
            }

            val success = Pictures.upload(args.first, message.attachments)

            if(success) {
                respond {
                    title = "Success!  Picture(s) uploaded."
                }
            } else {
                respond {
                    title = "Failure.  Ping bot owner to fix the bot."
                }
            }
        }
    }
    command("categories") {
        description = "Get the list of available categories of pictures.  Example: &categories"
        execute {
            val dirs = File("pictures/").list()!!
            val stringBuilder = StringBuilder()
            for (dir in dirs) {
                stringBuilder.append("`").append(dir).append("`\n")
            }
            respond {
                title = "Categories"
                description = stringBuilder.toString()
                color = Color(0, 255, 0)
            }
        }
    }
    command("post") {
        description = "Get a picture.  Example: &post staff"
        execute(AnyArg) {
            channel.createMessage {
                addFile(Pictures.randomPicture(args.first).toPath())
            }
        }
    }
}

@Suppress("unused")
fun quotesCommands() = commands("Quotes") {
    command("createquote", "cq", "cp") {
        description = "Create a quote.  Takes two quote arguments, Content and Author.  Example: &createquote \"content\" \"author\""
        execute(QuoteArg, QuoteArg) {
            val quoteNumber = Quotes.createQuote(args.first, args.second)
            respond {
                title = "Created quote #$quoteNumber"
                description = String.format("%s - %s", args.first, args.second)
                color = Color(0, 255, 0)
            }
            if(quoteNumber.toInt() % 100 == 0) {
                channel.createMessage {
                    embed {
                        title = "You have earned the quotes file, being that you are now at $quoteNumber quotes."
                    }
                    addFile(Path("./quotes.json"))
                }
            }
        }
    }
    command("quote") {
        description = "Get a quote.  Quotes are found by number.  Example: &quote 1"
        execute(IntegerArg) {
            val quote = Quotes.findQuote(args.first)
            if(quote != null) {
                respond {
                    title = "Quote #" + quote.number
                    description = String.format("%s - %s", quote.content, quote.author)
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
    command("randomquote") {
        description = "Get a random quote.  Example: &randomquote"
        execute {
            val quote = Quotes.findQuote(random.nextInt(Quotes.quoteTotal()-1)+1)!!
            respond {
                title = "Quote #" + quote.number
                description = String.format("%s - %s", quote.content, quote.author)
                color = Color(0, 255, 0)
            }
        }
    }
    command("search") {
        description = "Search for a phrase in the quotes file.  Optional second argument to search by author name.  Example: &search \"foo\" false"
        execute(QuoteArg, BooleanArg.optional(false)) {
            val quotes = Quotes.search(args.first, args.second).take(20)
            val stringBuilder = StringBuilder()
            for (quote in quotes) {
                stringBuilder.append("#").append(quote.number).append(": ").append(quote.content).append(" - ").append(quote.author).append("\n")
            }
            if(quotes.isNotEmpty()) {
                respond {
                    title = "Found entries for search term \"" + args.first + "\""
                    description = stringBuilder.toString()
                    color = Color(0, 255, 0)
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
fun utilityCommands() = commands("Utility", Permissions.BOT_OWNER) {
    command("shutdown") {
        description = "Shut down the bot."
        execute {
            respond {
                title = "Shutting down in five seconds..."
                color = Color(255, 0, 0)
            }
            delay(5000)
            message.kord.shutdown()
            exitProcess(0)
        }
    }
    command("quotesfile") {
        description = "Get the quotes file from the bot."
        execute {
            channel.createMessage {
                embed {
                    title = "The quote file for you..."
                }
                addFile(Path("./quotes.json"))
            }
        }
    }
}