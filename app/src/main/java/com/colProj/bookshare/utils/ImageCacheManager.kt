package com.colProj.bookshare.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object ImageCacheManager {

    suspend fun downloadAndCacheImage(context: Context, imageUrl: String, filename: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (imageUrl.isBlank()) return@withContext null

                // Ensure securing http -> https if needed
                val secureUrl = imageUrl.replace("http:", "https:")

                val cacheDir = File(context.filesDir, "images")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                // Append .jpg or generic extension
                val safeFilename = "$filename.jpg"
                val file = File(cacheDir, safeFilename)

                // If it already exists, avoid downloading again
                if (file.exists() && file.length() > 0) {
                    return@withContext file.absolutePath
                }

                val url = URL(secureUrl)
                val connection = url.openConnection()
                connection.connect()

                connection.getInputStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
