package com.studies.rrbmustudies.domain.repository

import com.studies.rrbmustudies.domain.model.Course
import com.studies.rrbmustudies.domain.model.CourseLevel
import com.studies.rrbmustudies.domain.model.CourseSystem
import com.studies.rrbmustudies.domain.model.Part
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun getCourses(): Flow<List<Course>>
    fun getAllCourses(): Flow<List<Course>>
    fun getCoursesByLevel(level: CourseLevel?): Flow<List<Course>>
    fun getCourse(courseId: String): Flow<Course?>
    fun getSystems(courseId: String): Flow<List<CourseSystem>>
    fun getParts(courseId: String, systemId: String): Flow<List<Part>>
    suspend fun createCourse(course: Course): Result<Course>
    suspend fun updateCourse(course: Course): Result<Course>
    suspend fun uploadCourseBackground(bytes: ByteArray, fileName: String, courseId: String): Result<String>
}
