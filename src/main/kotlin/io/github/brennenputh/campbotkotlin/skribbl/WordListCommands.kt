package io.github.brennenputh.campbotkotlin.skribbl

import dev.kord.core.behavior.interaction.respondPublic
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.commands.commands
import java.io.File

val wordListFile = File("wordlist.csv")

@Suppress("unused")
fun wordListCommands() = commands("skribbl") {
    slash("wordlist") {
        execute {
            val wordList = wordListFile.readText()
            interaction?.respondPublic {
                content = "Current skribbl word list: ${wordList.ifEmpty { "No words yet." }}"
            }
        }
    }
    slash("wordlist-add") {
        execute(AnyArg("word")) {
            wordListFile.appendText("${args.first},")
            interaction?.respondPublic {
                content = "Added ${args.first} to the word list."
            }
        }
    }
}