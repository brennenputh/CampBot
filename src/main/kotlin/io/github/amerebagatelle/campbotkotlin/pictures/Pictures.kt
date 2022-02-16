@file:OptIn(ExperimentalSerializationApi::class)

package io.github.amerebagatelle.campbotkotlin.pictures

import dev.kord.core.entity.Attachment
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.amerebagatelle.campbotkotlin.EMBED_GREEN
import io.github.amerebagatelle.campbotkotlin.EMBED_RED
import io.github.amerebagatelle.campbotkotlin.getDataDirectory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Path
import kotlin.random.Random
import kotlin.random.nextInt

fun getCategories(): Array<String> = getDataDirectory().resolve("pictures").toFile().listFiles()!!.filter { it.isDirectory }.map { it.name }.toTypedArray()

class FailedToUploadException : Exception()

fun upload(category: String, picture: Attachment) {
    try {
        val filePath = getDataDirectory().resolve("pictures").resolve(category).resolve("${System.currentTimeMillis()}${picture.filename}")
        URL(picture.url).openStream().use { input ->
            FileOutputStream(filePath.toFile()).use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        throw FailedToUploadException()
    }
}

fun uploadWithMessage(category: String, picture: Attachment): suspend (EmbedBuilder) -> Unit = {
    try {
        upload(category, picture)
        it.apply {
            title = "Success!  File uploaded."
            color = EMBED_GREEN
        }
    } catch (e: FailedToUploadException) {
        it.apply {
            title = "AAAAAAAAAAAAAAAAAAAAAA EVERYONE PANIC SOMETHING WENT VERY VERY VERY VERY VERY VERY VERY VERY VERY VERY VERY WRONG"
            color = EMBED_RED
        }
    }
}

// first is the path of the picture in storage, second is the url
val pictureCacheMap = mutableListOf<Picture>()

fun loadPictureCache() {
    pictureCacheMap.addAll(Json.decodeFromStream<List<Picture>>(FileInputStream(File("${getDataDirectory()}/pictures/cache.json"))))
}

val jsonFormat = Json {
    prettyPrint = true
}

fun syncPictureCache() {
    jsonFormat.encodeToStream(pictureCacheMap.toList(), FileOutputStream(File("${getDataDirectory()}/pictures/cache.json")))
}

private val recentlyPostedPicturesMap = getCategories().associateWith { mutableListOf<Int>() }

fun randomPicture(category: String): Picture {
    val files = getDataDirectory().resolve("pictures").resolve(category).toFile().listFiles()!!
    val recentlyPostedPictures = recentlyPostedPicturesMap[category] ?: mutableListOf(1)

    var selectedFileIndex = Random.Default.nextInt(files.indices)
    while (recentlyPostedPictures.contains(selectedFileIndex)) {
        selectedFileIndex++
        selectedFileIndex %= files.size
    }

    recentlyPostedPictures.add(selectedFileIndex)
    if (recentlyPostedPictures.size > files.size / 2) recentlyPostedPictures.clear()

    return pictureCacheMap.find { it.path == files[selectedFileIndex].toPath() } ?: Picture(files[selectedFileIndex].toPath(), null)
}

object PathAsStringSerializer : KSerializer<Path> {
    override val descriptor = PrimitiveSerialDescriptor("Path", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Path) = encoder.encodeString(value.toAbsolutePath().toString())

    override fun deserialize(decoder: Decoder): Path = Path.of(decoder.decodeString())
}

@Serializable
data class Picture(@Serializable(with = PathAsStringSerializer::class) val path: Path, val url: String?)