package com.studies.rrbmustudies.data.remote.firestore

import com.studies.rrbmustudies.data.dto.CourseDto
import com.studies.rrbmustudies.data.dto.PartDto
import com.studies.rrbmustudies.data.dto.PaperDto
import com.studies.rrbmustudies.data.dto.SystemDto
import com.studies.rrbmustudies.data.remote.CourseRemoteDataSource
import com.studies.rrbmustudies.data.remote.FirestorePaths
import com.studies.rrbmustudies.data.remote.RemotePaperRef
import com.studies.rrbmustudies.data.remote.parsePaperDocumentPath
import com.studies.rrbmustudies.data.remote.PaperRemoteDataSource
import com.studies.rrbmustudies.data.remote.SearchRemoteDataSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class FirestoreCourseDataSource(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : CourseRemoteDataSource {

    override fun observeCourses(): Flow<List<Pair<String, CourseDto>>> =
        firestore.collection(FirestorePaths.COURSES)
            .orderBy("order", Direction.ASCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.data<CourseDto>()?.let { doc.id to it }
                }
            }

    override fun observeCourse(courseId: String): Flow<CourseDto?> =
        firestore.collection(FirestorePaths.COURSES)
            .document(courseId)
            .snapshots
            .map { it.data<CourseDto>() }

    override fun observeSystems(courseId: String): Flow<List<Pair<String, SystemDto>>> =
        firestore.collection(FirestorePaths.COURSES)
            .document(courseId)
            .collection(FirestorePaths.SYSTEMS)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.data<SystemDto>()?.let { doc.id to it }
                }
            }

    override fun observeParts(
        courseId: String,
        systemId: String,
    ): Flow<List<Pair<String, PartDto>>> =
        firestore.collection(FirestorePaths.COURSES)
            .document(courseId)
            .collection(FirestorePaths.SYSTEMS)
            .document(systemId)
            .collection(FirestorePaths.PARTS)
            .orderBy("order", Direction.ASCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.data<PartDto>()?.let { doc.id to it }
                }
            }

    override suspend fun saveCourse(courseId: String, course: CourseDto) {
        firestore.collection(FirestorePaths.COURSES)
            .document(courseId)
            .set(course, merge = true)
    }

    override suspend fun seedCourseSkeleton(courseId: String) {
        // Read durationYears when present so 2-year courses only get Semesters 1–4.
        val courseSnap = firestore.collection(FirestorePaths.COURSES).document(courseId).get()
        val durationYears = courseSnap.data<CourseDto>()?.durationYears?.coerceIn(1, 6) ?: 3
        val semesterCount = (durationYears * 2).coerceIn(2, 8)

        val systems = listOf(
            Triple("yearly_system", "Yearly System", "YEARLY"),
            Triple("semester_system", "Semester System", "SEMESTER"),
            Triple("entrance_exam", "Entrance Exam", "ENTRANCE"),
        )
        for ((systemId, systemName, systemType) in systems) {
            firestore.collection(FirestorePaths.COURSES)
                .document(courseId)
                .collection(FirestorePaths.SYSTEMS)
                .document(systemId)
                .set(SystemDto(name = systemName, type = systemType), merge = true)

            val parts = when (systemId) {
                "yearly_system" -> listOf(
                    "part_1" to PartDto(name = "Part 1", order = 1),
                    "part_2" to PartDto(name = "Part 2", order = 2),
                    "part_3" to PartDto(name = "Part 3", order = 3),
                )
                "semester_system" -> (1..semesterCount).map { sem ->
                    "semester_$sem" to PartDto(name = "Semester $sem", order = sem)
                }
                else -> listOf("entrance_papers" to PartDto(name = "Entrance Papers", order = 1))
            }
            for ((partId, part) in parts) {
                firestore.collection(FirestorePaths.COURSES)
                    .document(courseId)
                    .collection(FirestorePaths.SYSTEMS)
                    .document(systemId)
                    .collection(FirestorePaths.PARTS)
                    .document(partId)
                    .set(part, merge = true)
            }
        }
    }
}

class FirestorePaperDataSource(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : PaperRemoteDataSource {

    private fun papersCollection(courseId: String, systemId: String, partId: String) =
        firestore.collection(FirestorePaths.COURSES)
            .document(courseId)
            .collection(FirestorePaths.SYSTEMS)
            .document(systemId)
            .collection(FirestorePaths.PARTS)
            .document(partId)
            .collection(FirestorePaths.PAPERS)

    override fun observePapers(
        courseId: String,
        systemId: String,
        partId: String,
        includeUnpublished: Boolean,
    ): Flow<List<Pair<String, PaperDto>>> =
        papersCollection(courseId, systemId, partId)
            .orderBy("year", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.data<PaperDto>()?.let { doc.id to it }
                }.filter { (_, dto) -> includeUnpublished || dto.isPublished }
            }

    override fun observePaper(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ): Flow<PaperDto?> =
        papersCollection(courseId, systemId, partId)
            .document(paperId)
            .snapshots
            .map { it.data<PaperDto>() }

    override fun observeRecentPapers(limit: Int): Flow<List<RemotePaperRef>> =
        // No server-side orderBy: it both requires a composite index AND silently drops
        // documents missing `createdAt`. We fetch published papers and sort client-side so
        // every published paper is eligible for "Recently Added".
        firestore.collectionGroup(FirestorePaths.PAPERS)
            .where { "isPublished" equalTo true }
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val dto = doc.data<PaperDto>() ?: return@mapNotNull null
                    val pathIds = parsePaperDocumentPath(doc.reference.path) ?: return@mapNotNull null
                    RemotePaperRef(
                        paperId = pathIds.paperId,
                        courseId = pathIds.courseId,
                        systemId = pathIds.systemId,
                        partId = pathIds.partId,
                        dto = dto,
                    )
                }
                    .sortedByDescending { it.dto.createdAt }
                    .take(limit)
            }
            .catch { emit(emptyList()) }

    override suspend fun incrementDownloadCount(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ) {
        val ref = papersCollection(courseId, systemId, partId).document(paperId)
        firestore.runTransaction {
            val current = get(ref).data<PaperDto>()
            if (current != null) {
                set(ref, current.copy(downloadCount = current.downloadCount + 1), merge = true)
            }
        }
    }

    override suspend fun savePaper(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
        paper: PaperDto,
    ) {
        papersCollection(courseId, systemId, partId)
            .document(paperId)
            .set(paper, merge = true)
    }

    override suspend fun deletePaper(
        courseId: String,
        systemId: String,
        partId: String,
        paperId: String,
    ) {
        papersCollection(courseId, systemId, partId)
            .document(paperId)
            .delete()
    }
}

class FirestoreSearchDataSource(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : SearchRemoteDataSource {

    override fun searchPapers(query: String, limit: Int): Flow<List<RemotePaperRef>> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return kotlinx.coroutines.flow.flowOf(emptyList())
        val needle = normalized.lowercase()

        // No server-side orderBy (avoids composite-index dependency and dropping docs without
        // createdAt). Load published papers, filter client-side by title/subject/code, then sort.
        return firestore.collectionGroup(FirestorePaths.PAPERS)
            .where { "isPublished" equalTo true }
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    val dto = doc.data<PaperDto>() ?: return@mapNotNull null
                    val pathIds = parsePaperDocumentPath(doc.reference.path) ?: return@mapNotNull null
                    val haystack = listOf(dto.title, dto.subject, dto.paperCode, dto.description.orEmpty())
                        .joinToString(" ")
                        .lowercase()
                    if (!haystack.contains(needle)) return@mapNotNull null
                    RemotePaperRef(
                        paperId = pathIds.paperId,
                        courseId = pathIds.courseId,
                        systemId = pathIds.systemId,
                        partId = pathIds.partId,
                        dto = dto,
                    )
                }
                    .sortedByDescending { it.dto.createdAt }
                    .take(limit)
            }
            .catch { emit(emptyList()) }
    }
}
