package com.studies.rrbmustudies.`data`.local

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class DownloadedPaperQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAllOrdered(mapper: (
    id: String,
    courseId: String,
    systemId: String,
    partId: String,
    title: String,
    subject: String,
    paperCode: String,
    year: Long,
    remotePdfUrl: String,
    localPath: String,
    fileSizeBytes: Long,
    downloadedAt: Long,
  ) -> T): Query<T> = Query(1_712_808_389, arrayOf("DownloadedPaper"), driver, "DownloadedPaper.sq",
      "selectAllOrdered",
      "SELECT DownloadedPaper.id, DownloadedPaper.courseId, DownloadedPaper.systemId, DownloadedPaper.partId, DownloadedPaper.title, DownloadedPaper.subject, DownloadedPaper.paperCode, DownloadedPaper.year, DownloadedPaper.remotePdfUrl, DownloadedPaper.localPath, DownloadedPaper.fileSizeBytes, DownloadedPaper.downloadedAt FROM DownloadedPaper ORDER BY downloadedAt DESC") {
      cursor ->
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
      cursor.getString(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!
    )
  }

  public fun selectAllOrdered(): Query<DownloadedPaper> = selectAllOrdered { id, courseId, systemId,
      partId, title, subject, paperCode, year, remotePdfUrl, localPath, fileSizeBytes,
      downloadedAt ->
    DownloadedPaper(
      id,
      courseId,
      systemId,
      partId,
      title,
      subject,
      paperCode,
      year,
      remotePdfUrl,
      localPath,
      fileSizeBytes,
      downloadedAt
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
    remotePdfUrl: String,
    localPath: String,
    fileSizeBytes: Long,
    downloadedAt: Long,
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
      cursor.getString(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!
    )
  }

  public fun selectById(id: String): Query<DownloadedPaper> = selectById(id) { id_, courseId,
      systemId, partId, title, subject, paperCode, year, remotePdfUrl, localPath, fileSizeBytes,
      downloadedAt ->
    DownloadedPaper(
      id_,
      courseId,
      systemId,
      partId,
      title,
      subject,
      paperCode,
      year,
      remotePdfUrl,
      localPath,
      fileSizeBytes,
      downloadedAt
    )
  }

  public fun count(): Query<Long> = Query(1_003_066_642, arrayOf("DownloadedPaper"), driver,
      "DownloadedPaper.sq", "count", "SELECT COUNT(*) FROM DownloadedPaper") { cursor ->
    cursor.getLong(0)!!
  }

  public fun totalBytes(): Query<Long> = Query(841_295_204, arrayOf("DownloadedPaper"), driver,
      "DownloadedPaper.sq", "totalBytes",
      "SELECT COALESCE(SUM(fileSizeBytes), 0) FROM DownloadedPaper") { cursor ->
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
    remotePdfUrl: String,
    localPath: String,
    fileSizeBytes: Long,
    downloadedAt: Long,
  ) {
    driver.execute(-1_358_880_549, """
        |INSERT OR REPLACE INTO DownloadedPaper(
        |    id, courseId, systemId, partId, title, subject, paperCode, year,
        |    remotePdfUrl, localPath, fileSizeBytes, downloadedAt
        |) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 12) {
          bindString(0, id)
          bindString(1, courseId)
          bindString(2, systemId)
          bindString(3, partId)
          bindString(4, title)
          bindString(5, subject)
          bindString(6, paperCode)
          bindLong(7, year)
          bindString(8, remotePdfUrl)
          bindString(9, localPath)
          bindLong(10, fileSizeBytes)
          bindLong(11, downloadedAt)
        }
    notifyQueries(-1_358_880_549) { emit ->
      emit("DownloadedPaper")
    }
  }

  public fun deleteById(id: String) {
    driver.execute(-957_592_070, """DELETE FROM DownloadedPaper WHERE id = ?""", 1) {
          bindString(0, id)
        }
    notifyQueries(-957_592_070) { emit ->
      emit("DownloadedPaper")
    }
  }

  public fun deleteAll() {
    driver.execute(-30_891_399, """DELETE FROM DownloadedPaper""", 0)
    notifyQueries(-30_891_399) { emit ->
      emit("DownloadedPaper")
    }
  }

  private inner class SelectByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("DownloadedPaper", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("DownloadedPaper", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-83_170_101,
        """SELECT DownloadedPaper.id, DownloadedPaper.courseId, DownloadedPaper.systemId, DownloadedPaper.partId, DownloadedPaper.title, DownloadedPaper.subject, DownloadedPaper.paperCode, DownloadedPaper.year, DownloadedPaper.remotePdfUrl, DownloadedPaper.localPath, DownloadedPaper.fileSizeBytes, DownloadedPaper.downloadedAt FROM DownloadedPaper WHERE id = ?""",
        mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "DownloadedPaper.sq:selectById"
  }
}
