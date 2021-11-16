package io.github.amerebagatelle.campbotkotlin.pictures

import dev.kord.core.entity.Attachment
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.random.Random
import kotlin.random.nextInt

class Pictures {
    companion object {
        fun upload(category: String, pictures: Set<Attachment>): Boolean {
            for (picture in pictures) {
                try {
                    URL(picture.url).openStream().use { input ->
                        FileOutputStream(File("pictures/" + category + "/" + System.currentTimeMillis() + picture.filename)).use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    return false
                }
            }

            return true
        }

        private val recentlyPostedPictures = mutableListOf<Int>()

        fun randomPicture(category: String): File {
            val files = File("pictures/$category/").listFiles()!!

            var selectedFileIndex = Random.Default.nextInt(files.indices)
            do {
                selectedFileIndex++
                selectedFileIndex %= files.size
            } while (recentlyPostedPictures.contains(selectedFileIndex))

            recentlyPostedPictures.add(selectedFileIndex)
            if (recentlyPostedPictures.size > files.size / 2) recentlyPostedPictures.clear()

            return files[selectedFileIndex]
        }
    }
}