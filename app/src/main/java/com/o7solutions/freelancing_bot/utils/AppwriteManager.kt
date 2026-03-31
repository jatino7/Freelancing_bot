package com.o7solutions.freelancing_bot.utils

import android.content.Context
import android.net.Uri
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AppwriteManager private constructor(private val context: Context) {

    //  Appwrite Config
    private val endpoint = "https://fra.cloud.appwrite.io/v1"
    private val projectId = "68402a5a000611723049"
    private val bucketId = "6996d375001a97008da8"


    private val client = Client(context)
        .setEndpoint(endpoint)
        .setProject(projectId)

    private val storage = Storage(client)

    companion object {
        @Volatile
        private var INSTANCE: AppwriteManager? = null

        fun getInstance(context: Context): AppwriteManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppwriteManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Upload image using URI and return public image URL
     */
    suspend fun uploadImageFromUri(imageUri: Uri): String =
        withContext(Dispatchers.IO) {

            val file = uriToFile(imageUri)

            val uploadedFile = storage.createFile(
                bucketId = bucketId,
                fileId = ID.unique(),
                file = InputFile.fromFile(file)
            )

            "$endpoint/storage/buckets/$bucketId/files/${uploadedFile.id}/view?project=$projectId"
        }


    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open URI")

        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)

        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output)
        }

        inputStream.close()
        return tempFile
    }
}
