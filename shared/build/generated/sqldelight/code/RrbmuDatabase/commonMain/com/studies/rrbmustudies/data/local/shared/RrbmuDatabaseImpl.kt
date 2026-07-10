package com.studies.rrbmustudies.`data`.local.shared

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.studies.rrbmustudies.`data`.local.CachedPaperQueries
import com.studies.rrbmustudies.`data`.local.RrbmuDatabase
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<RrbmuDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = RrbmuDatabaseImpl.Schema

internal fun KClass<RrbmuDatabase>.newInstance(driver: SqlDriver): RrbmuDatabase =
    RrbmuDatabaseImpl(driver)

private class RrbmuDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), RrbmuDatabase {
  override val cachedPaperQueries: CachedPaperQueries = CachedPaperQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE CachedPaper (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    courseId TEXT NOT NULL,
          |    systemId TEXT NOT NULL,
          |    partId TEXT NOT NULL,
          |    title TEXT NOT NULL,
          |    subject TEXT NOT NULL,
          |    paperCode TEXT NOT NULL,
          |    year INTEGER NOT NULL,
          |    pdfUrl TEXT NOT NULL,
          |    coverImageUrl TEXT,
          |    cachedAt INTEGER NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, "CREATE INDEX cached_paper_viewed ON CachedPaper(cachedAt DESC)", 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
