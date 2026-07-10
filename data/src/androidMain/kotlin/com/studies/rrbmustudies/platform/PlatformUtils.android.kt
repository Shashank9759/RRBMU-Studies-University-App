package com.studies.rrbmustudies.platform

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun generateId(): String = java.util.UUID.randomUUID().toString()
