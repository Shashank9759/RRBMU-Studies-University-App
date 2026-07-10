package com.studies.rrbmustudies.data.remote

interface StorageRemoteDataSource {
    suspend fun uploadFile(path: String, bytes: ByteArray, contentType: String): String
}
