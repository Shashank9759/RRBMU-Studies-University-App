package com.studies.rrbmustudies.domain.model

/**
 * Skeleton seed data for all 31 courses.
 * Used by the Firestore seed script and as fallback reference.
 */
object CourseSeed {

  data class SeedCourse(
      val id: String,
      val name: String,
      val shortName: String,
      val level: CourseLevel,
      val order: Int,
  )

  val courses: List<SeedCourse> = listOf(
      SeedCourse(CourseIds.BSC, "Bachelor of Science", "B.Sc", CourseLevel.UG, 1),
      SeedCourse(CourseIds.BA, "Bachelor of Arts", "B.A", CourseLevel.UG, 2),
      SeedCourse(CourseIds.BCOM, "Bachelor of Commerce", "B.Com", CourseLevel.UG, 3),
      SeedCourse(CourseIds.BBA, "Bachelor of Business Administration", "BBA", CourseLevel.UG, 4),
      SeedCourse(CourseIds.BCA, "Bachelor of Computer Applications", "BCA", CourseLevel.UG, 5),
      SeedCourse(CourseIds.B_ED, "Bachelor of Education", "B.Ed", CourseLevel.UG, 6),
      SeedCourse(CourseIds.BALLB, "Bachelor of Arts LLB", "BA LLB", CourseLevel.UG, 7),
      SeedCourse(CourseIds.BDS, "Bachelor of Dental Surgery", "BDS", CourseLevel.UG, 8),
      SeedCourse(CourseIds.BE, "Bachelor of Engineering", "B.E", CourseLevel.UG, 9),
      SeedCourse(CourseIds.BHM, "Bachelor of Hotel Management", "BHM", CourseLevel.UG, 10),
      SeedCourse(CourseIds.BHSC, "Bachelor of Health Science", "BHSc", CourseLevel.UG, 11),
      SeedCourse(CourseIds.BPED, "Bachelor of Physical Education", "B.P.Ed", CourseLevel.UG, 12),
      SeedCourse(CourseIds.BSCBED, "B.Sc B.Ed", "B.Sc B.Ed", CourseLevel.UG, 13),
      SeedCourse(CourseIds.BSW, "Bachelor of Social Work", "BSW", CourseLevel.UG, 14),
      SeedCourse(CourseIds.LLB, "Bachelor of Laws", "LLB", CourseLevel.UG, 15),
      SeedCourse(CourseIds.MA, "Master of Arts", "M.A", CourseLevel.PG, 16),
      SeedCourse(CourseIds.MBA, "Master of Business Administration", "MBA", CourseLevel.PG, 17),
      SeedCourse(CourseIds.MBBS, "Bachelor of Medicine & Surgery", "MBBS", CourseLevel.UG, 18),
      SeedCourse(CourseIds.MCA, "Master of Computer Applications", "MCA", CourseLevel.PG, 19),
      SeedCourse(CourseIds.MCOM, "Master of Commerce", "M.Com", CourseLevel.PG, 20),
      SeedCourse(CourseIds.MD, "Doctor of Medicine", "MD", CourseLevel.PG, 21),
      SeedCourse(CourseIds.MDS, "Master of Dental Surgery", "MDS", CourseLevel.PG, 22),
      SeedCourse(CourseIds.MED, "Master of Education", "M.Ed", CourseLevel.PG, 23),
      SeedCourse(CourseIds.MPED, "Master of Physical Education", "M.P.Ed", CourseLevel.PG, 24),
      SeedCourse(CourseIds.MPHIL, "Master of Philosophy", "M.Phil", CourseLevel.PG, 25),
      SeedCourse(CourseIds.MS, "Master of Surgery", "MS", CourseLevel.PG, 26),
      SeedCourse(CourseIds.MSC, "Master of Science", "M.Sc", CourseLevel.PG, 27),
      SeedCourse(CourseIds.MSW, "Master of Social Work", "MSW", CourseLevel.PG, 28),
      SeedCourse(CourseIds.BABED, "BA B.Ed", "BA B.Ed", CourseLevel.UG, 29),
      SeedCourse(CourseIds.DIPLOMA, "Diploma Courses", "Diploma", CourseLevel.DIPLOMA, 30),
      SeedCourse(CourseIds.PGDCA, "Post Graduate Diploma in Computer Applications", "PGDCA", CourseLevel.DIPLOMA, 31),
  )

  val systems: List<CourseSystem> = courses.flatMap { course ->
      listOf(
          CourseSystem(SystemIds.YEARLY, course.id, "Yearly System", SystemType.YEARLY),
          CourseSystem(SystemIds.SEMESTER, course.id, "Semester System", SystemType.SEMESTER),
          CourseSystem(SystemIds.ENTRANCE, course.id, "Entrance Exam", SystemType.ENTRANCE),
      )
  }

  val yearlyParts: List<Part> = courses.flatMap { course ->
      listOf(
          Part("part_1", course.id, SystemIds.YEARLY, "Part 1", order = 1),
          Part("part_2", course.id, SystemIds.YEARLY, "Part 2", order = 2),
          Part("part_3", course.id, SystemIds.YEARLY, "Part 3", order = 3),
      )
  }

  val semesterParts: List<Part> = courses.flatMap { course ->
      (1..6).map { sem ->
          Part("semester_$sem", course.id, SystemIds.SEMESTER, "Semester $sem", order = sem)
      }
  }

  val entranceParts: List<Part> = courses.map { course ->
      Part("entrance_papers", course.id, SystemIds.ENTRANCE, "Entrance Papers", order = 1)
  }

  val allParts: List<Part> = yearlyParts + semesterParts + entranceParts

  fun toCourse(seed: SeedCourse): Course = Course(
      id = seed.id,
      name = seed.name,
      shortName = seed.shortName,
      order = seed.order,
      level = seed.level,
  )
}
