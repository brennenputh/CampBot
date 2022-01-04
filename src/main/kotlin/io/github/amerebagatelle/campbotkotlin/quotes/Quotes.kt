package io.github.amerebagatelle.campbotkotlin.quotes

import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.amerebagatelle.campbotkotlin.EMBED_GREEN
import io.github.amerebagatelle.campbotkotlin.getErrorEmbed
import java.io.File
import java.io.FileReader
import java.io.FileWriter

private val quoteFile = File("./quotes.json")

fun findQuote(number: Int): Quote? {
    var author: String? = null
    var content: String? = null
    var quotedBy: String? = null
    JsonReader(FileReader(quoteFile)).use { reader ->
        reader.beginObject {
            while (reader.hasNext()) {
                if (reader.nextName() == number.toString()) {
                    reader.beginObject {
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "author" -> author = reader.nextString()
                                "content" -> content = reader.nextString()
                                "quotedBy" -> quotedBy = reader.nextString()
                            }
                        }
                    }
                } else {
                    reader.beginObject {
                        while (reader.hasNext()) {
                            reader.nextName()
                        }
                    }
                }
            }
        }
    }
    if (author == null || content == null) return null

    return Quote(number, author!!, content!!, quotedBy ?: "Unknown")
}

fun createQuote(author: String, content: String, quotedBy: String = "Unknown"): Int {
    val quoteNumber = quoteTotal() + 1

    val json = Klaxon().parseJsonObject(FileReader(quoteFile))

    json[quoteNumber.toString()] = mapOf(Pair("author", author), Pair("content", content), Pair("quotedBy", quotedBy))

    val writer = FileWriter(quoteFile)
    writer.write(json.toJsonString(true))
    writer.close()

    return quoteNumber
}

fun createQuoteWithMessage(author: String, content: String, quotedBy: String = "Unknown"): suspend (EmbedBuilder) -> Unit {
    val quoteNumber = createQuote(author, content, quotedBy)
    return {
        it.apply {
            title = "Created quote #$quoteNumber"
            description = "$author - $content"
            color = EMBED_GREEN
        }
    }
}

fun quoteTotal(): Int {
    var quoteNumber = 0
    JsonReader(FileReader(quoteFile)).use { reader ->
        reader.beginObject {
            while (reader.hasNext()) {
                val newNumber = Integer.parseInt(reader.nextName())
                if (newNumber > quoteNumber) {
                    quoteNumber = newNumber
                }
                reader.beginObject {
                    while (reader.hasNext()) reader.nextName()
                }
            }
        }
    }
    return quoteNumber
}

fun search(searchTerm: String): List<Quote> {
    val quotes = mutableListOf<Pair<Int, Quote>>()
    JsonReader(FileReader(quoteFile)).use { reader ->
        reader.beginObject {
            while (reader.hasNext()) {
                val number = Integer.parseInt(reader.nextName())
                reader.beginObject {
                    var author = ""
                    var content = ""
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "author" -> author = reader.nextString()
                            "content" -> content = reader.nextString()
                        }
                    }
                    val relevance = FuzzySearch.tokenSetRatio(content, searchTerm)
                    if (relevance > 50 || author.contains(searchTerm, true)) quotes.add(
                        Pair(relevance, Quote(number, author, content))
                    )
                }
            }
        }
    }
    return quotes.sortedBy { it.first }.map { it.second }
}

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

class Quote(val number: Int, val author: String, val content: String, val quotedBy: String = "")