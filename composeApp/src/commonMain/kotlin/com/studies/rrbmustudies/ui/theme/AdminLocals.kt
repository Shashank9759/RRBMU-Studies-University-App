package com.studies.rrbmustudies.ui.theme

import androidx.compose.runtime.compositionLocalOf
import com.studies.rrbmustudies.domain.model.AdminUser

val LocalIsAdmin = compositionLocalOf { false }
val LocalAdminUser = compositionLocalOf<AdminUser?> { null }
