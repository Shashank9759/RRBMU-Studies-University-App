package com.studies.rrbmustudies.platform

import android.app.Activity

private var activityRef: Activity? = null

fun bindActivity(activity: Activity) {
    activityRef = activity
}

fun currentActivity(): Activity? = activityRef
