package tk.fishfish.dataflow.database

import org.apache.spark.sql.Row
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.jdbc.support.JdbcUtils
import tk.fishfish.dataflow.entity.enums.JdbcProperty
import tk.fishfish.dataflow.util.{CollectionUtils, Properties, Validation}

import java.sql.{Connection, DriverManager, PreparedStatement}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
 * 通用SQL数据管理
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
abstract class SqlDataHub(props: Properties) extends DataHub {

  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  protected val url: String = {
    val url = props.getString(JdbcProperty.URL.key())
    Validation.nonEmpty(url, "jdbc url参数不能为空")
    url
  }
  protected val user: String = props.getString(JdbcProperty.USER.key())
  protected val password: String = props.getString(JdbcProperty.PASSWORD.key())
  protected val batch: Int = props.getInt(JdbcProperty.BATCH.key(), 200)

  override def insert(table: String, columns: Seq[String], rows: Iterator[Row]): Unit = {
    if (CollectionUtils.isEmpty(rows) || CollectionUtils.isEmpty(columns)) {
      return
    }
    val sql =
      s"""
         |INSERT INTO $table ${columns.mkString("(", ", ", ")")} VALUES ${columns.map(_ => "?").mkString("(", ", ", ")")}
         |""".stripMargin.replaceAll("\n", " ")
    logger.info("执行SQL: {}", sql)
    var con: Connection = null
    var ps: PreparedStatement = null
    try {
      con = getCon
      con.setAutoCommit(false)
      ps = con.prepareStatement(sql)
      var batch = 0
      for (row <- rows) {
        batch = batch + 1
        var i = 0
        for (column <- columns) {
          val value: Any = row.getAs(column)
          ps.setObject(i + 1, value)
          i = i + 1
        }
        ps.addBatch()
        if (batch == this.batch) {
          ps.executeBatch()
          con.commit()
          batch = 0
        }
      }
      ps.executeBatch()
      con.commit()
    } finally {
      JdbcUtils.closeStatement(ps)
      JdbcUtils.closeConnection(con)
    }
  }

  override def update(table: String, columns: Seq[String], rows: Iterator[Row]): Unit = {
    if (CollectionUtils.isEmpty(rows) || CollectionUtils.isEmpty(columns)) {
      return
    }
    val sql =
      s"""
         |INSERT INTO $table ${columns.mkString("(", ", ", ")")} VALUES ${columns.map(_ => "?").mkString("(", ", ", ")")}
         |ON DUPLICATE KEY UPDATE ${columns.map(column => s"$column = ?").mkString(", ")}
         |""".stripMargin.replaceAll("\n", " ")
    logger.info("执行SQL: {}", sql)
    var con: Connection = null
    var ps: PreparedStatement = null
    try {
      con = getCon
      con.setAutoCommit(false)
      ps = con.prepareStatement(sql)
      var batch = 0
      for (row <- rows) {
        batch = batch + 1
        var i = 0
        for (column <- columns) {
          val value: Any = row.getAs(column)
          ps.setObject(i + 1, value)
          ps.setObject(i + 1 + columns.size, value)
          i = i + 1
        }
        ps.addBatch()
        if (batch == this.batch) {
          ps.executeBatch()
          con.commit()
          batch = 0
        }
      }
      ps.executeBatch()
      con.commit()
    } finally {
      JdbcUtils.closeStatement(ps)
      JdbcUtils.closeConnection(con)
    }
  }

  protected def getCon: Connection = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Await.result(Future {
      DriverManager.getConnection(url, user, password)
    }, 5.seconds)
  }

}
