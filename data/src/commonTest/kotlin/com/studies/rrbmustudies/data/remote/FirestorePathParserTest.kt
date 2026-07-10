package com.studies.rrbmustudies.data.remote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FirestorePathParserTest {

    @Test
    fun parsePaperDocumentPath_extractsIds() {
        val path = "courses/bsc/systems/yearly_system/parts/part_1/papers/paper_abc"
        val result = parsePaperDocumentPath(path)
        assertEquals(
            PaperPathIds("bsc", "yearly_system", "part_1", "paper_abc"),
            result,
        )
    }

    @Test
    fun parsePaperDocumentPath_handlesLeadingSlash() {
        val path = "/courses/ba/systems/semester_system/parts/semester_2/papers/xyz"
        val result = parsePaperDocumentPath(path)
        assertEquals(
            PaperPathIds("ba", "semester_system", "semester_2", "xyz"),
            result,
        )
    }

    @Test
    fun parsePaperDocumentPath_returnsNullForInvalidPath() {
        assertNull(parsePaperDocumentPath("courses/bsc/systems/yearly"))
        assertNull(parsePaperDocumentPath("invalid/path"))
    }
}
