package com.studies.rrbmustudies.platform

import platform.Foundation.NSUUID

actual fun currentTimeMillis(): Long =
    (platform.Foundation.NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun generateId(): String = NSUUID().UUIDString()
