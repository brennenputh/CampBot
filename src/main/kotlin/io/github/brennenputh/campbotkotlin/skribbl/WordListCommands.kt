package io.github.brennenputh.campbotkotlin.skribbl

import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.commands.commands
import java.io.File

val wordListFile = File("wordlist.csv")

@Suppress("unused")
fun wordListCommands() = commands("skribbl") {
    slash("wordlist") {
        execute {
            val wordList = wordListFile.readText()
            respond("Current skribbl word list: ${wordList.ifEmpty { "No words yet." }}")
        }
    }
    slash("wordlist-add") {
        execute(AnyArg("word")) {
            wordListFile.appendText("${args.first},")
            respond("Added ${args.first} to the word list.")
        }
    }
}