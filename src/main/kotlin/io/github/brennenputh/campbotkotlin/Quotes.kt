@file:OptIn(ExperimentalSerializationApi::class)

package io.github.brennenputh.campbotkotlin

import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.Emojis
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.fullName
import kotlin.math.floor
import kotlin.random.Random

@Suppress("unused")
fun quoteSlashCommands() = commands("Quotes") {
    slash("createquote", description = "Create a quote.") {
        execute(AnyArg(name = "content"), AnyArg(name = "author")) {
            respondPublic("", createQuoteWithMessage(args.second, args.first, author.id))
        }
    }

    message("Create Quote", "createquotemessage", "Create a quote with a message.") {
        val authorName = args.first.author?.id?.let { getInfo(it) }?.realName ?: run {
            respond("", getErrorEmbed("Could not find author.  Is this a bot or webhook?"))
            return@message
        }
        if (authorName.isEmpty()) {
            respond(getErrorEmbed("No recorded name for this user.\nAuthor must run `&updateInfo realName (name)` first."))
            return@message
        }
        respondPublic("", createQuoteWithMessage(authorName, args.first.content, author.id))
    }

    slash("quote", description = "Get a quote.") {
        execute(IntegerArg(name = "quoteNumber")) {
            respondPublic("", getQuoteMessageForNumber(args.first, author.kord))
        }
    }

    slash("randomquote", description = "Get a random quote.") {
        execute {
            respondPublic("", getQuoteMessageForNumber(Random.nextInt(quoteTotal()) + 1, author.kord))
        }
    }

    slash("search", description = "Search for a phrase in the quotes file.") {
        execute(AnyArg("phrase")) {
            val quotes = search(args.first)
            if (quotes.isNotEmpty()) {
                interaction?.respondPublic {
                    content = "Searching..."
                }
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
                respondPublic("") {
                    title = "No results found."
                    description = "Did not find any matches for your query."
                    color = EMBED_RED
                }
            }
        }
    }

    slash(
        "editquote",
        description = "Edit a quote.  This only works if you are the author of the quote you are editing."
    ) {
        execute(IntegerArg("quoteNumber"), AnyArg("content").optionalNullable(), AnyArg("author").optionalNullable()) {
            if (findQuote(args.first)?.isAuthor(author) == true) {
                editQuote(args.first, args.second, args.third)
                val newQuote = findQuote(args.first)!!
                respondPublic {
                    title = "Quote Edited: #${args.first}"
                    description = newQuote.content + " - " + newQuote.author
                }
            } else {
                respond("", getErrorEmbed("You are not the recorded author of this quote."))
            }
        }
    }
}

private val quoteFile = getDataDirectory().resolve("quotes.json").toFile()

private fun getQuotes(): List<Quote> = Json.decodeFromStream(quoteFile.inputStream())

fun findQuote(number: Int): Quote? = getQuotes().find { it.number == number }

fun createQuote(author: String, content: String, quotedBy: Snowflake = Snowflake.min): Int {
    val quoteNumber = quoteTotal() + 1
    val quote = Quote(quoteNumber, author, content, quotedBy)

    val quotes = getQuotes().toMutableList()
    quotes.add(quote)

    Json.encodeToStream(quotes.toList(), quoteFile.outputStream())

    return quoteNumber
}

fun createQuoteWithMessage(author: String, content: String, quotedBy: Snowflake): suspend (EmbedBuilder) -> Unit {
    val quoteNumber = createQuote(author, content, quotedBy)
    return {
        it.apply {
            title = "Created quote #$quoteNumber"
            description = "$content - $author"
            color = EMBED_GREEN
        }
    }
}

fun editQuote(quoteNumber: Int, newContent: String? = null, newAuthor: String? = null) {
    val quotes = getQuotes().toMutableList()
    val quote = quotes.filter { it.number == quoteNumber }[0]
    val newQuote = Quote(quoteNumber, newAuthor ?: quote.author, newContent ?: quote.content, quote.quotedBy)
    quotes.remove(quote)
    quotes.add(newQuote)

    Json.encodeToStream(quotes.toList(), quoteFile.outputStream())
}

private fun quoteTotal() = getQuotes().maxOfOrNull { it.number } ?: 0

fun search(searchTerm: String): List<Quote> = getQuotes().filter {
    it.author.contains(searchTerm, true) || FuzzySearch.tokenSetRatio(
        it.content,
        searchTerm
    ) > 50
}.sortedBy { FuzzySearch.tokenSetRatio(it.content, searchTerm) }

fun getQuoteMessage(quote: Quote, kord: Kord): suspend (EmbedBuilder) -> Unit = {
    it.apply {
        title = "Quote #${quote.number}"
        description = "${quote.content} - ${quote.author}"
        footer {
            text = "Quoted by: ${kord.getUser(quote.quotedBy)?.fullName ?: "Unknown"}"
        }
        color = EMBED_GREEN
    }
}

fun getQuoteMessageForNumber(number: Int, kord: Kord): suspend (EmbedBuilder) -> Unit =
    findQuote(number)?.let { getQuoteMessage(it, kord) } ?: getErrorEmbed("Quote not found.")

@Serializable
class Quote(val number: Int, val author: String, val content: String, val quotedBy: Snowflake) {
    fun isAuthor(user: User): Boolean = user.id == quotedBy
}