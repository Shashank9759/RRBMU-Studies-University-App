package com.studies.rrbmustudies.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class CourseSeedTest {

    @Test
    fun seed_containsAll31Courses() {
        assertEquals(31, CourseSeed.courses.size)
        assertEquals(31, CourseIds.ALL.size)
    }

    @Test
    fun seed_eachCourseHasThreeSystems() {
        assertEquals(31 * 3, CourseSeed.systems.size)
        CourseSeed.courses.forEach { course ->
            val systems = CourseSeed.systems.filter { it.courseId == course.id }
            assertEquals(3, systems.size)
        }
    }

    @Test
    fun seed_yearlyHasThreeParts_perCourse() {
        assertEquals(31 * 3, CourseSeed.yearlyParts.size)
    }

    @Test
    fun seed_semesterHasSixParts_perCourse() {
        assertEquals(31 * 6, CourseSeed.semesterParts.size)
    }
}
