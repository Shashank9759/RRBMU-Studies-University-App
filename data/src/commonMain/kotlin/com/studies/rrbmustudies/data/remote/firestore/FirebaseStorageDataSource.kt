package com.studies.rrbmustudies.data.remote.firestore

import com.studies.rrbmustudies.data.remote.StorageRemoteDataSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.FirebaseStorage
import dev.gitlive.firebase.storage.storage
import dev.gitlive.firebase.storage.storageMetadata

class FirebaseStorageDataSource(
    private val storage: FirebaseStorage = Firebase.storage,
) : StorageRemoteDataSource {

    override suspend fun uploadFile(path: String, bytes: ByteArray, contentType: String): String {
        val ref = storage.reference.child(path)
        // Persist the MIME type so download URLs are served as images/PDFs (not octet-stream).
        ref.putData(Data(bytes), storageMetadata { this.contentType = contentType })
        return ref.getDownloadUrl()
    }
}
