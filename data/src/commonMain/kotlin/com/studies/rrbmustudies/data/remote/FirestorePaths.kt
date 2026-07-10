package com.studies.rrbmustudies.data.remote

object FirestorePaths {
    const val COURSES = "courses"
    const val SYSTEMS = "systems"
    const val PARTS = "parts"
    const val PAPERS = "papers"
    const val HOME_ADS = "home_ads"
    const val APP_SETTINGS = "app_settings"
    const val HOME_CAROUSEL_SETTINGS = "home_carousel"
    const val NOTIFICATIONS = "notifications"
    const val FEEDBACK = "feedback"
    const val ADMINS = "admins"

    fun coursePath(courseId: String) = "$COURSES/$courseId"
    fun systemPath(courseId: String, systemId: String) =
        "$COURSES/$courseId/$SYSTEMS/$systemId"
    fun partPath(courseId: String, systemId: String, partId: String) =
        "$COURSES/$courseId/$SYSTEMS/$systemId/$PARTS/$partId"
    fun paperPath(courseId: String, systemId: String, partId: String, paperId: String) =
        "$COURSES/$courseId/$SYSTEMS/$systemId/$PARTS/$partId/$PAPERS/$paperId"
}
