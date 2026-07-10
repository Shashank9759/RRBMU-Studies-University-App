package com.studies.rrbmustudies.domain.repository

import com.studies.rrbmustudies.domain.model.HomeAd
import com.studies.rrbmustudies.domain.model.HomeCarouselSettings
import kotlinx.coroutines.flow.Flow

interface HomeAdRepository {
    fun getActiveAds(): Flow<List<HomeAd>>
    fun getAllAds(): Flow<List<HomeAd>>
    fun observeCarouselSettings(): Flow<HomeCarouselSettings>
    suspend fun saveCarouselSettings(settings: HomeCarouselSettings): Result<Unit>
    suspend fun createAd(ad: HomeAd): Result<HomeAd>
    suspend fun updateAd(ad: HomeAd): Result<HomeAd>
    suspend fun reorderAds(orderedAds: List<HomeAd>): Result<Unit>
    suspend fun deleteAd(adId: String): Result<Unit>
    /** Upload banner bytes to Firebase Storage; returns public download URL. */
    suspend fun uploadAdImage(bytes: ByteArray, fileName: String): Result<String>
}
