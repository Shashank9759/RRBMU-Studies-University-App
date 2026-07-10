package com.studies.rrbmustudies.`data`.local

import kotlin.Long
import kotlin.String

public data class CachedPaper(
  public val id: String,
  public val courseId: String,
  public val systemId: String,
  public val partId: String,
  public val title: String,
  public val subject: String,
  public val paperCode: String,
  public val year: Long,
  public val pdfUrl: String,
  public val coverImageUrl: String?,
  public val cachedAt: Long,
)
