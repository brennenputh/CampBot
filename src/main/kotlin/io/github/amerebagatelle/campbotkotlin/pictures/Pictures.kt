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

/**
 * @param category The category of the picture
 * @param picture The attachment
 *
 * @return Whether uploading the picture succeeded
 */
fun upload(category: String, picture: Attachment): Boolean {
    return try {
        val filePath = getDataDirectory().resolve("pictures").resolve(category).resolve("${System.currentTimeMillis()}${picture.filename}")
        URL(picture.url).openStream().use { input ->
            FileOutputStream(filePath.toFile()).use { output ->
                input.copyTo(output)
            }
        }
        true
    } catch (e: Exception) { false }
}

fun uploadWithMessage(category: String, pictures: Set<Attachment>): suspend (EmbedBuilder) -> Unit = {
    for (picture in pictures) {
        if (!upload(category, picture)) {
            it.apply {
                title = "Could not upload picture: ${picture.filename}"
                color = EMBED_RED
            }
        } else {
            it.apply {
                title = "Success!  File (${picture.filename}) uploaded."
                color = EMBED_GREEN
            }
        }
    }
}

private val pictureCache = mutableListOf<Picture>()

private val json = Json { prettyPrint = true }

fun loadPictureCache() {
    pictureCache.clear()
    pictureCache.addAll(json.decodeFromStream<List<Picture>>(FileInputStream(File("${getDataDirectory()}/pictures/cache.json"))))
}

fun addToPictureCache(picture: Picture) {
    pictureCache.add(picture)
    json.encodeToStream(pictureCache.toList(), FileOutputStream(File("${getDataDirectory()}/pictures/cache.json")))
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

    return pictureCache.find { it.path == files[selectedFileIndex].toPath() } ?: Picture(files[selectedFileIndex].toPath(), null)
}

object PathAsStringSerializer : KSerializer<Path> {
    override val descriptor = PrimitiveSerialDescriptor("Path", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Path) = encoder.encodeString(value.toAbsolutePath().toString())

    override fun deserialize(decoder: Decoder): Path = Path.of(decoder.decodeString())
}

@Serializable
data class Picture(@Serializable(with = PathAsStringSerializer::class) val path: Path, val url: String?)