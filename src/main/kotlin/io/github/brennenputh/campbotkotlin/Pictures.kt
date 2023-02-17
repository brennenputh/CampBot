@file:OptIn(ExperimentalSerializationApi::class)

package io.github.brennenputh.campbotkotlin

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Attachment
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.jakejmattson.discordkt.arguments.AttachmentArg
import me.jakejmattson.discordkt.arguments.ChoiceArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.pluralize
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.random.Random
import kotlin.random.nextInt

@Suppress("unused")
fun pictureCommands() = commands("Pictures") {
    slash("upload", description = "Upload a file to the bot.  Expects an attachment.  Example: &upload staff") {
        execute(
            ChoiceArg("category", "The category the picture should go in.", choices = getCategories()),
            AttachmentArg("picture")
        ) {
            if (!getCategories().contains(args.first)) {
                respond(getErrorEmbed("That category does not exist."))
                return@execute
            }

            respondPublic("", uploadWithMessage(args.first, args.second))
        }
    }

    slash(
        "post",
        description = "Get a file.  Append number to the end for posting more than one (limit 20).  Example: &post staff 1"
    ) {
        execute(
            ChoiceArg("category", "The category the picture should go in.", choices = getCategories()),
            IntegerArg("number", "The number of pictures the bot should post.").optional(1)
        ) {
            if (args.second > 50) {
                respond(getErrorEmbed("Too many files requested.  Limit is 50."))
                return@execute
            }
            if (!getCategories().contains(args.first)) {
                respond(getErrorEmbed("That category does not exist."))
                return@execute
            }

            respondPublic("Posting ${args.second.pluralize("picture")}...")
            repeat(args.second) {
                delay(3000)
                val pic = randomPicture(args.first)
                val response = channel.createMessage {
                    if (pic.url != null) {
                        content = pic.url
                    } else {
                        addFile(pic.path)
                    }
                }
                if (pic.url == null) addToPictureCache(Picture(pic.path, response.attachments.first().url))
            }
        }
    }
}

val picturesDirectory: Path = getDataDirectory().resolve("pictures")

private fun getCategories(): Array<String> =
    picturesDirectory.toFile().listFiles()!!.filter { it.isDirectory }.map { it.name }.toTypedArray()

/**
 * @param category The category of the picture
 * @param picture The attachment
 *
 * @return Whether uploading the picture succeeded
 */
private fun upload(category: String, picture: Attachment): Boolean {
    return try {
        val filePath = picturesDirectory.resolve(category).resolve("${System.currentTimeMillis()}${picture.filename}")
        URL(picture.url).openStream().use { input ->
            FileOutputStream(filePath.toFile()).use { output ->
                input.copyTo(output)
            }
        }
        true
    } catch (e: Exception) {
        false
    }
}

private fun uploadWithMessage(category: String, picture: Attachment): suspend (EmbedBuilder) -> Unit = {
    if (!upload(category, picture)) {
        getErrorEmbed("Failed to upload: ${picture.filename}")
    } else {
        it.apply {
            title = "Success!  Picture uploaded to category `$category`."
            color = EMBED_GREEN
            image = picture.url
        }
    }
}

private val pictureCache = mutableListOf<Picture>()

private val json = Json { prettyPrint = true }

fun loadPictureCache() {
    pictureCache.clear()
    pictureCache.addAll(
        json.decodeFromStream<List<Picture>>(
            FileInputStream(
                picturesDirectory.resolve("cache.json").toFile()
            )
        )
    )

    // Scan to make sure those pictures haven't been deleted
    pictureCache.removeIf { !it.path.exists() }
    json.encodeToStream(pictureCache.toList(), FileOutputStream(picturesDirectory.resolve("cache.json").toFile()))
}

private fun addToPictureCache(picture: Picture) {
    pictureCache.add(picture)
    json.encodeToStream(pictureCache.toList(), FileOutputStream(picturesDirectory.resolve("cache.json").toFile()))
}

private val recentlyPostedPicturesMap = getCategories().associateWith { mutableListOf<Int>() }

private fun randomPicture(category: String): Picture {
    val files = picturesDirectory.resolve(category).toFile().listFiles()!!
    val recentlyPostedPictures = recentlyPostedPicturesMap[category] ?: mutableListOf(1)

    var selectedFileIndex = Random.Default.nextInt(files.indices)
    while (recentlyPostedPictures.contains(selectedFileIndex)) {
        selectedFileIndex++
        selectedFileIndex %= files.size
    }

    recentlyPostedPictures.add(selectedFileIndex)
    if (recentlyPostedPictures.size > files.size / 2) recentlyPostedPictures.clear()

    return pictureCache.find { it.path == files[selectedFileIndex].toPath() }
        ?: Picture(files[selectedFileIndex].toPath(), null)
}

@Serializable
data class Picture(@Serializable(with = PathAsStringSerializer::class) val path: Path, val url: String?)