package io.github.amerebagatelle.campbotkotlin.features

import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class Quotes {
    companion object {
        private val quoteFile = File("./quotes.json")

        fun findQuote(number: Int): Quote? {
            var author: String? = null
            var content: String? = null
            JsonReader(FileReader(quoteFile)).use { reader ->
                reader.beginObject {
                    while(reader.hasNext()) {
                        if(reader.nextName() == number.toString()) {
                            reader.beginObject {
                                while (reader.hasNext()) {
                                    when (reader.nextName()) {
                                        "author" -> author = reader.nextString()
                                        "content" -> content = reader.nextString()
                                    }
                                }
                            }
                        } else {
                            reader.beginObject {
                                while(reader.hasNext()) {
                                    reader.nextName()
                                }
                            }
                        }
                    }
                }
            }
            if(author == null || content == null) return null

            return Quote(number, author!!, content!!)
        }

        fun createQuote(author: String, content: String): Number {
            val quoteNumber = quoteTotal() + 1

            val json = Klaxon().parseJsonObject(FileReader(quoteFile))

            json[quoteNumber.toString()] = mapOf(Pair("author", author), Pair("content", content))

            val writer = FileWriter(quoteFile)
            writer.write(json.toJsonString(true))
            writer.close()

            return quoteNumber
        }

        fun quoteTotal(): Int {
            var quoteNumber = 0
            JsonReader(FileReader(quoteFile)).use { reader ->
                reader.beginObject {
                    while(reader.hasNext()) {
                        val newNumber = Integer.parseInt(reader.nextName())
                        if(newNumber > quoteNumber) {
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
            val quotes = mutableListOf<Quote>()
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
                            if(if(!byAuthor) FuzzySearch.tokenSetRatio(content, searchTerm) > 50 else author.lowercase() == searchTerm.lowercase()) quotes.add(Quote(number, author, content))
                        }
                    }
                }
            }

            return quotes.sortedBy { quote -> quote.number }
        }
    }

    class Quote(val number: Int, val author: String, val content: String)
}