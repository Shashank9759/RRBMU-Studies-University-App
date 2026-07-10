package com.studies.rrbmustudies.data.remote

import com.studies.rrbmustudies.data.dto.CourseDto
import com.studies.rrbmustudies.data.dto.PartDto
import com.studies.rrbmustudies.data.dto.PaperDto
import com.studies.rrbmustudies.data.dto.SystemDto
import kotlinx.coroutines.flow.Flow

interface CourseRemoteDataSource {
    fun observeCourses(): Flow<List<Pair<String, CourseDto>>>
    fun observeCourse(courseId: String): Flow<CourseDto?>
    fun observeSystems(courseId: String): Flow<List<Pair<String, SystemDto>>>
    fun observeParts(courseId: String, systemId: String): Flow<List<Pair<String, PartDto>>>
    suspend fun saveCourse(courseId: String, course: CourseDto)
    suspend fun seedCourseSkeleton(courseId: String)
}

interface PaperRemoteDataSource {
    fun observePapers(
        courseId: String,
        systemId: String,
        partId: String,
        includeUnpublished: Boolean,
    ): Flow<List<Pair<String, PaperDto>>>

    fun observePaper(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): Flow<PaperDto?>

    fun observeRecentPapers(limit: Int): Flow<List<RemotePaperRef>>

    suspend fun incrementDownloadCount(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    )

    suspend fun savePaper(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
        paper: PaperDto,
    )

    suspend fun deletePaper(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    )
}

interface SearchRemoteDataSource {
    fun searchPapers(query: String, limit: Int): Flow<List<RemotePaperRef>>
}
