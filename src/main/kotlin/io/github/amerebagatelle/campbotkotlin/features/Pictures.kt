package io.github.amerebagatelle.campbotkotlin.features

import dev.kord.core.entity.Attachment
import java.io.File
import java.io.FileOutputStream
import java.net.URL

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
                } catch(e: Exception) {
                    return false
                }
            }

            return true
        }

        fun randomPicture(category: String): File {
            return File("pictures/$category/").listFiles()!!.random()
        }
    }
}