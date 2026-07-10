package com.studies.rrbmustudies.`data`.local

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.studies.rrbmustudies.`data`.local.shared.newInstance
import com.studies.rrbmustudies.`data`.local.shared.schema
import kotlin.Unit

public interface RrbmuDatabase : Transacter {
  public val cachedPaperQueries: CachedPaperQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = RrbmuDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): RrbmuDatabase =
        RrbmuDatabase::class.newInstance(driver)
  }
}
