package com.studies.rrbmustudies.data.mapper

import com.studies.rrbmustudies.data.dto.CourseDto
import com.studies.rrbmustudies.data.dto.PaperDto
import com.studies.rrbmustudies.data.dto.SystemDto
import com.studies.rrbmustudies.domain.model.CourseLevel
import com.studies.rrbmustudies.domain.model.SystemType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MappersTest {

    @Test
    fun courseDto_mapsToDomain_withCorrectLevel() {
        val dto = CourseDto(
            name = "Bachelor of Science",
            shortName = "B.Sc",
            order = 1,
            isActive = true,
            level = "UG",
        )
        val domain = dto.toDomain("bsc")
        assertEquals("bsc", domain.id)
        assertEquals("B.Sc", domain.shortName)
        assertEquals(CourseLevel.UG, domain.level)
        assertTrue(domain.isActive)
    }

    @Test
    fun paperDto_roundTripsThroughDomain() {
        val dto = PaperDto(
            title = "BSC-PART-1-CHEMISTRY-706-A-2023",
            subject = "Chemistry",
            paperCode = "706-A",
            year = 2023,
            pdfUrl = "https://example.com/paper.pdf",
            isPublished = true,
            downloadCount = 42,
        )
        val domain = dto.toDomain("p1", "bsc", "yearly_system", "part_1")
        val back = domain.toDto()
        assertEquals(dto.title, back.title)
        assertEquals(dto.subject, back.subject)
        assertEquals(dto.paperCode, back.paperCode)
        assertEquals(dto.year, back.year)
        assertEquals(dto.downloadCount, back.downloadCount)
    }

    @Test
    fun systemType_mapsCorrectly() {
        val yearly = SystemDto(name = "Yearly", type = "YEARLY")
            .toDomain("yearly_system", "bsc")
        assertEquals(SystemType.YEARLY, yearly.type)

        val semester = SystemDto(name = "Semester", type = "SEMESTER")
            .toDomain("semester_system", "bsc")
        assertEquals(SystemType.SEMESTER, semester.type)
    }
}
