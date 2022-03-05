package io.github.brennenputh.campbotkotlin

import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder

val EMBED_GREEN = Color(0x00FF00)
val EMBED_RED = Color(0xFF0000)

fun getErrorEmbed(message: String): suspend (EmbedBuilder) -> Unit = {
    it.apply {
        title = "Error"
        description = message
        color = EMBED_RED
    }
}