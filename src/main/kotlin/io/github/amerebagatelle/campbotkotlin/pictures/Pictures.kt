package io.github.amerebagatelle.campbotkotlin.pictures

import dev.kord.core.entity.Attachment
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.amerebagatelle.campbotkotlin.EMBED_GREEN
import io.github.amerebagatelle.campbotkotlin.EMBED_RED
import io.github.amerebagatelle.campbotkotlin.getDataDirectory
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Path
import kotlin.random.Random
import kotlin.random.nextInt

fun getCategories(): Array<String> = getDataDirectory().resolve("pictures").toFile().list()!!

fun upload(category: String, pictures: Set<Attachment>) {
    for (picture in pictures) {
        try {
            URL(picture.url).openStream().use { input ->
                FileOutputStream(getDataDirectory().resolve("pictures").resolve(category).resolve("${System.currentTimeMillis()}${picture.filename}").toFile()).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            throw FailedToUploadException()
        }
    }
}

class FailedToUploadException : Exception()

fun uploadWithMessage(category: String, pictures: Set<Attachment>): suspend (EmbedBuilder) -> Unit = {
    try {
        upload(category, pictures)
        it.apply {
            title = "Success!  File(s) uploaded."
            color = EMBED_GREEN
        }
    } catch (e: FailedToUploadException) {
        it.apply {
            title = "AAAAAAAAAAAAAAAAAAAAAA EVERYONE PANIC SOMETHING WENT VERY VERY VERY VERY VERY VERY VERY VERY VERY VERY VERY WRONG"
            color = EMBED_RED
        }
    }
}

private val recentlyPostedPicturesMap = getCategories().associateWith { mutableListOf<Int>() }

fun randomPicture(category: String): Path {
    val files = getDataDirectory().resolve("pictures").resolve(category).toFile().listFiles()!!
    val recentlyPostedPictures = recentlyPostedPicturesMap[category] ?: mutableListOf(1)

    var selectedFileIndex = Random.Default.nextInt(files.indices)
    while (recentlyPostedPictures.contains(selectedFileIndex)) {
        selectedFileIndex++
        selectedFileIndex %= files.size
    }

    recentlyPostedPictures.add(selectedFileIndex)
    if (recentlyPostedPictures.size > files.size / 2) recentlyPostedPictures.clear()
    return files[selectedFileIndex].toPath()
}