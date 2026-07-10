package com.studies.rrbmustudies.data.repository

import com.studies.rrbmustudies.data.mapper.toDomain
import com.studies.rrbmustudies.data.mapper.toDto
import com.studies.rrbmustudies.data.remote.CourseRemoteDataSource
import com.studies.rrbmustudies.data.remote.StorageRemoteDataSource
import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.CourseLevel
import com.studies.rrbmustudies.domain.model.CourseSeed
import com.studies.rrbmustudies.domain.model.CourseSystem
import com.studies.rrbmustudies.domain.model.Part
import com.studies.rrbmustudies.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class CourseRepositoryImpl(
    private val remote: CourseRemoteDataSource,
    private val storage: StorageRemoteDataSource,
) : CourseRepository {

    private val bundledCourses: List<Course> by lazy {
        CourseSeed.courses.map { CourseSeed.toCourse(it) }.sortedBy { it.order }
    }

    private fun mapCourses(docs: List<Pair<String, com.studies.rrbmustudies.data.dto.CourseDto>>): List<Course> =
        docs.map { (id, dto) -> dto.toDomain(id) }.sortedBy { it.order }

    override fun getAllCourses(): Flow<List<Course>> =
        remote.observeCourses()
            .map(::mapCourses)
            .catch { emit(bundledCourses) }

    override fun getCourses(): Flow<List<Course>> =
        getAllCourses().map { courses -> courses.filter { it.isActive } }

    override fun getCoursesByLevel(level: CourseLevel?): Flow<List<Course>> =
        getCourses().map { list ->
            if (level == null) list else list.filter { it.level == level }
        }

    override fun getCourse(courseId: String): Flow<Course?> =
        remote.observeCourse(courseId)
            .map { dto -> dto?.toDomain(courseId)?.takeIf { it.isActive } }
            .catch { emit(bundledCourses.find { it.id == courseId }) }

    override fun getSystems(courseId: String): Flow<List<CourseSystem>> =
        remote.observeSystems(courseId)
            .map { docs -> docs.map { (id, dto) -> dto.toDomain(id, courseId) } }
            .catch { emit(CourseSeed.systems.filter { it.courseId == courseId }) }

    override fun getParts(courseId: String, systemId: String): Flow<List<Part>> =
        remote.observeParts(courseId, systemId)
            .map { docs ->
                docs.map { (id, dto) -> dto.toDomain(id, courseId, systemId) }
                    .sortedBy { it.order }
            }
            .catch {
                emit(
                    CourseSeed.allParts
                        .filter { it.courseId == courseId && it.systemId == systemId }
                        .sortedBy { it.order },
                )
            }

    override suspend fun createCourse(course: Course): Result<Course> = runCatching {
        val id = course.id.trim().lowercase()
        require(id.isNotBlank()) { "Course ID is required" }
        val created = course.copy(id = id)
        remote.saveCourse(id, created.toDto())
        remote.seedCourseSkeleton(id)
        created
    }

    override suspend fun updateCourse(course: Course): Result<Course> = runCatching {
        require(course.id.isNotBlank()) { "Course ID is required" }
        remote.saveCourse(course.id, course.toDto())
        course
    }

    override suspend fun uploadCourseBackground(
        bytes: ByteArray,
        fileName: String,
        courseId: String,
    ): Result<String> = runCatching {
        require(bytes.isNotEmpty()) { "Image is empty" }
        val ext = fileName.substringAfterLast('.', "jpg").lowercase().ifBlank { "jpg" }
        val contentType = when (ext) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        val path = "course_backgrounds/${courseId.trim().lowercase()}.$ext"
        storage.uploadFile(path, bytes, contentType)
    }
}
