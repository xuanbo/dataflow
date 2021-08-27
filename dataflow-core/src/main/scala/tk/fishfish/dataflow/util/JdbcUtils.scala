package tk.fishfish.dataflow.util

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet, Statement, Timestamp}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
 * jdbc工具类
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object JdbcUtils {

  val mysqlPrefix = "jdbc:mysql://"
  val oraclePrefix = "jdbc:oracle:"
  val dmPrefix = "jdbc:dm://"
  val postgrePrefix = "jdbc:postgresql://"
  val sqlserverPrefix = "jdbc:sqlserver://"

  def getCon(url: String, user: String, password: String, timeout: Int = 5): Connection = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Await.result(Future {
      DriverManager.getConnection(url, user, password)
    }, timeout.seconds)
  }

  def setParameterValue(ps: PreparedStatement, index: Int, value: Any): Unit = {
    value match {
      case null => {
        try {
          // 防止驱动不支持获取参数类型
          val sqlType = ps.getParameterMetaData.getParameterType(index)
          ps.setNull(index, sqlType)
        } catch {
          case _: Exception => ps.setObject(index, null)
        }
      }
      case str: String => ps.setString(index, str)
      case timestamp: Timestamp => ps.setTimestamp(index, timestamp)
      case _ => ps.setObject(index, value)
    }
  }

  def query[T](connectionProvider: => Connection, sql: String, resultSetExtractor: ResultSet => T): T = {
    var con: Connection = null
    var stmt: Statement = null
    var rs: ResultSet = null
    try {
      con = connectionProvider
      stmt = con.createStatement()
      rs = stmt.executeQuery(sql)
      resultSetExtractor(rs)
    } finally {
      closeResultSet(rs)
      closeStatement(stmt)
      closeConnection(con)
    }
  }

  def queryList(connectionProvider: => Connection, sql: String): Seq[Map[String, Any]] =
    query(connectionProvider, sql, resultSetExtractor)

  def resultSetExtractor(rs: ResultSet): Seq[Map[String, Any]] = {
    val metadata = rs.getMetaData
    val buffer = new ListBuffer[Map[String, Any]]()
    while (rs.next()) {
      val map = mutable.Map[String, Any]()
      for (i <- 1 to metadata.getColumnCount) {
        map += (metadata.getColumnName(i) -> rs.getObject(i))
      }
      buffer += map.toMap
    }
    buffer
  }

  def stringColumnResultSetExtractor(column: String)(rs: ResultSet): Seq[String] = {
    val buffer = new ListBuffer[String]()
    while (rs.next()) {
      buffer += rs.getString(column)
    }
    buffer
  }

  def executeBatch(connectionProvider: => Connection, sql: String, batchArgs: Iterator[Seq[Any]],
                   batchSize: Int = 4096): Unit = {
    var con: Connection = null
    var ps: PreparedStatement = null
    try {
      con = connectionProvider
      con.setAutoCommit(false)
      ps = con.prepareStatement(sql)
      var index = 0
      for (args <- batchArgs) {
        index += 1
        var i = 1
        for (arg <- args) {
          JdbcUtils.setParameterValue(ps, i, arg)
          i += 1
        }
        ps.addBatch()
        if (index == batchSize) {
          ps.executeBatch()
          con.commit()
          ps.clearBatch()
          index = 0
        }
      }
      if (index > 0) {
        ps.executeBatch()
        con.commit()
        ps.clearBatch()
      }
    } finally {
      JdbcUtils.closeStatement(ps)
      JdbcUtils.closeConnection(con)
    }
  }

  def closeResultSet(rs: ResultSet): Unit = close(rs)

  def closeStatement(stmt: Statement): Unit = close(stmt)

  def closeConnection(con: Connection): Unit = close(con)

  def close(closeable: AutoCloseable): Unit = if (closeable != null) {
    try {
      closeable.close()
    } catch {
      case _: Exception =>
    }
  }

  def using[R](con: Connection)(f: Connection => R): R = {
    try {
      f(con)
    } finally {
      closeConnection(con)
    }
  }

  def using[R](st: Statement)(f: Statement => R): R = {
    try {
      f(st)
    } finally {
      closeStatement(st)
    }
  }

}
