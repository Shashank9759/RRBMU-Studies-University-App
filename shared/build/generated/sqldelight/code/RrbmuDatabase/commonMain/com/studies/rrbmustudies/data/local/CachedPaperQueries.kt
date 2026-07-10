package com.studies.rrbmustudies.`data`.local

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class CachedPaperQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAllOrdered(limit: Long, mapper: (
    id: String,
    courseId: String,
    systemId: String,
    partId: String,
    title: String,
    subject: String,
    paperCode: String,
    year: Long,
    pdfUrl: String,
    coverImageUrl: String?,
    cachedAt: Long,
  ) -> T): Query<T> = SelectAllOrderedQuery(limit) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!,
      cursor.getString(8)!!,
      cursor.getString(9),
      cursor.getLong(10)!!
    )
  }

  public fun selectAllOrdered(limit: Long): Query<CachedPaper> = selectAllOrdered(limit) { id,
      courseId, systemId, partId, title, subject, paperCode, year, pdfUrl, coverImageUrl,
      cachedAt ->
    CachedPaper(
      id,
      courseId,
      systemId,
      partId,
      title,
      subject,
      paperCode,
      year,
      pdfUrl,
      coverImageUrl,
      cachedAt
    )
  }

  public fun <T : Any> selectById(id: String, mapper: (
    id: String,
    courseId: String,
    systemId: String,
    partId: String,
    title: String,
    subject: String,
    paperCode: String,
    year: Long,
    pdfUrl: String,
    coverImageUrl: String?,
    cachedAt: Long,
  ) -> T): Query<T> = SelectByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!,
      cursor.getLong(7)!!,
      cursor.getString(8)!!,
      cursor.getString(9),
      cursor.getLong(10)!!
    )
  }

  public fun selectById(id: String): Query<CachedPaper> = selectById(id) { id_, courseId, systemId,
      partId, title, subject, paperCode, year, pdfUrl, coverImageUrl, cachedAt ->
    CachedPaper(
      id_,
      courseId,
      systemId,
      partId,
      title,
      subject,
      paperCode,
      year,
      pdfUrl,
      coverImageUrl,
      cachedAt
    )
  }

  public fun count(): Query<Long> = Query(1_325_292_237, arrayOf("CachedPaper"), driver,
      "CachedPaper.sq", "count", "SELECT COUNT(*) FROM CachedPaper") { cursor ->
    cursor.getLong(0)!!
  }

  public fun insertOrReplace(
    id: String,
    courseId: String,
    systemId: String,
    partId: String,
    title: String,
    subject: String,
    paperCode: String,
    year: Long,
    pdfUrl: String,
    coverImageUrl: String?,
    cachedAt: Long,
  ) {
    driver.execute(298_989_782, """
        |INSERT OR REPLACE INTO CachedPaper(
        |    id, courseId, systemId, partId, title, subject, paperCode, year, pdfUrl, coverImageUrl, cachedAt
        |) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 11) {
          bindString(0, id)
          bindString(1, courseId)
          bindString(2, systemId)
          bindString(3, partId)
          bindString(4, title)
          bindString(5, subject)
          bindString(6, paperCode)
          bindLong(7, year)
          bindString(8, pdfUrl)
          bindString(9, coverImageUrl)
          bindLong(10, cachedAt)
        }
    notifyQueries(298_989_782) { emit ->
      emit("CachedPaper")
    }
  }

  public fun deleteOlderThan(threshold: Long) {
    driver.execute(-56_745_048, """DELETE FROM CachedPaper WHERE cachedAt < ?""", 1) {
          bindLong(0, threshold)
        }
    notifyQueries(-56_745_048) { emit ->
      emit("CachedPaper")
    }
  }

  private inner class SelectAllOrderedQuery<out T : Any>(
    public val limit: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CachedPaper", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CachedPaper", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_567_181_098,
        """SELECT CachedPaper.id, CachedPaper.courseId, CachedPaper.systemId, CachedPaper.partId, CachedPaper.title, CachedPaper.subject, CachedPaper.paperCode, CachedPaper.year, CachedPaper.pdfUrl, CachedPaper.coverImageUrl, CachedPaper.cachedAt FROM CachedPaper ORDER BY cachedAt DESC LIMIT ?""",
        mapper, 1) {
      bindLong(0, limit)
    }

    override fun toString(): String = "CachedPaper.sq:selectAllOrdered"
  }

  private inner class SelectByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CachedPaper", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CachedPaper", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(841_188_336,
        """SELECT CachedPaper.id, CachedPaper.courseId, CachedPaper.systemId, CachedPaper.partId, CachedPaper.title, CachedPaper.subject, CachedPaper.paperCode, CachedPaper.year, CachedPaper.pdfUrl, CachedPaper.coverImageUrl, CachedPaper.cachedAt FROM CachedPaper WHERE id = ?""",
        mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "CachedPaper.sq:selectById"
  }
}
