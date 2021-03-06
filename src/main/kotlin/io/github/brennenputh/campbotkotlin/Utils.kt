package io.github.brennenputh.campbotkotlin

import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.nio.file.Path

object PathAsStringSerializer : KSerializer<Path> {
    override val descriptor = PrimitiveSerialDescriptor("Path", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Path) = encoder.encodeString(value.toAbsolutePath().toString())

    override fun deserialize(decoder: Decoder): Path = Path.of(decoder.decodeString())
}

val EMBED_GREEN = Color(0x00FF00)
val EMBED_RED = Color(0xFF0000)

fun getErrorEmbed(message: String): suspend (EmbedBuilder) -> Unit = {
    it.apply {
        title = "Error"
        description = message
        color = EMBED_RED
    }
}

suspend inline fun <T : Any> Flow<T>.any(crossinline predicate: suspend (T) -> Boolean): Boolean =
    filter { predicate(it) }.firstOrNull() != null