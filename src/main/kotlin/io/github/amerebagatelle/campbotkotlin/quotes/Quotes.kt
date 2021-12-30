package io.github.amerebagatelle.campbotkotlin.quotes

import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
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

fun search(searchTerm: String, byAuthor: Boolean): List<Quote> {
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
                    if (if (!byAuthor) relevance > 50 else author.contains(searchTerm, true)) quotes.add(
                        Pair(relevance, Quote(number, author, content))
                    )
                }
            }
        }
    }
    return quotes.sortedBy { it.first }.map { it.second }
}

fun getQuoteMessage(quote: Quote): EmbedBuilder = EmbedBuilder().apply {
    title = "Quote # ${quote.number}"
    description = "${quote.content} - ${quote.author}"
    footer {
        text = "Quoted by: ${quote.quotedBy}"
    }
    color = Color(0, 255, 0)
}

fun getQuoteMessageForNumber(number: Int): EmbedBuilder = findQuote(number)?.let { getQuoteMessage(it) } ?: EmbedBuilder().apply {
    title = "Error"
    description = "Quote #$number not found"
    color = Color(255, 0, 0)
}

class Quote(val number: Int, val author: String, val content: String, val quotedBy: String = "")