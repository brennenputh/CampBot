@file:OptIn(ExperimentalSerializationApi::class)

package io.github.brennenputh.campbotkotlin.quotes

import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.brennenputh.campbotkotlin.EMBED_GREEN
import io.github.brennenputh.campbotkotlin.getDataDirectory
import io.github.brennenputh.campbotkotlin.getErrorEmbed
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

private val quoteFile = getDataDirectory().resolve("quotes.json").toFile()

fun getQuotes(): List<Quote> = Json.decodeFromStream(quoteFile.inputStream())

fun findQuote(number: Int): Quote? = getQuotes().find { it.number == number }

fun createQuote(author: String, content: String, quotedBy: String = "Unknown"): Int {
    val quoteNumber = quoteTotal() + 1
    val quote = Quote(quoteNumber, author, content, quotedBy)

    val quotes = getQuotes().toMutableList()
    quotes.add(quote)

    Json.encodeToStream(quotes.toList(), quoteFile.outputStream())

    return quoteNumber
}

fun createQuoteWithMessage(author: String, content: String, quotedBy: String = "Unknown"): suspend (EmbedBuilder) -> Unit {
    val quoteNumber = createQuote(author, content, quotedBy)
    return {
        it.apply {
            title = "Created quote #$quoteNumber"
            description = "$content - $author"
            color = EMBED_GREEN
        }
    }
}

fun quoteTotal() = getQuotes().maxOfOrNull { it.number } ?: 0

fun search(searchTerm: String): List<Quote> = getQuotes().filter { it.author.contains(searchTerm, true) || FuzzySearch.tokenSetRatio(it.content, searchTerm) > 50 }

fun getQuoteMessage(quote: Quote): suspend (EmbedBuilder) -> Unit = {
    it.apply {
        title = "Quote #${quote.number}"
        description = "${quote.content} - ${quote.author}"
        footer {
            text = "Quoted by: ${quote.quotedBy}"
        }
        color = EMBED_GREEN
    }
}

fun getQuoteMessageForNumber(number: Int): suspend (EmbedBuilder) -> Unit = findQuote(number)?.let { getQuoteMessage(it) } ?: getErrorEmbed("Quote not found.")

fun autocompleteNames() = listOf(
    "Brennen",
    "Audrey",
    "Adam",
    "Allison",
    "Grace",
    "Gracie",
    "Grace C",
    "Lindsay",
    "Ethan",
    "Jackson",
    "Matthew",
    "Christian",
    "Kevin",
    "Zach",
    "Camden",
    "Andrew",
    "Daniel",
    "Eli",
    "Harrison",
    "Isabelle",
    "Isaiah",
    "Josh",
    "Lydia",
    "Sophia",
    "Olivia",
    "Sadie",
    "Maya",
    "Steven",
    "Anonymous"
)

@Serializable
class Quote(val number: Int, val author: String, val content: String, val quotedBy: String = "")