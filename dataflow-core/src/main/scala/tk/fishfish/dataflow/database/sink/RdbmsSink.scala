package tk.fishfish.dataflow.database.sink

import org.apache.spark.sql.{DataFrame, Row}
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.util.{CollectionUtils, JdbcOptions, JdbcUtils, Properties, Validation}

import java.sql.Connection

/**
 * 关系型数据库目标端
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
abstract class RdbmsSink(val props: Properties) extends Sink {

  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  protected val url: String = {
    val url = props.getString(JdbcOptions.URL)
    Validation.nonEmpty(url, "jdbc url参数不能为空")
    url
  }
  protected val user: String = props.getString(JdbcOptions.USER)
  protected val password: String = props.getString(JdbcOptions.PASSWORD)
  protected val batchSize: Int = props.getInt(JdbcOptions.BATCH_SIZE, 4096)

  protected def getCon: Connection = JdbcUtils.getCon(url, user, password)

  override def write(df: DataFrame): Unit = {
    val table = props.getString(JdbcOptions.TABLE)
    val mode = props.getString(JdbcOptions.WRITE_MODE, "update")
    logger.info(s"写入表: $table, JDBC url: $url")

    val columns = df.schema.map(_.name)
    df.foreachPartition { rows =>
      mode match {
        case "insert" => insert(table, columns, rows)
        case "update" => update(table, columns, rows)
        case _ => throw new UnsupportedOperationException(s"不支持的${JdbcOptions.WRITE_MODE}: $mode")
      }
    }
  }

  protected def insert(table: String, columns: Seq[String], rows: Iterator[Row]): Unit = {
    if (CollectionUtils.isEmpty(rows) || CollectionUtils.isEmpty(columns)) {
      logger.warn("字段列为空或数据为空，跳过数据写入")
      return
    }
    val sql = insertSql(table, columns)
    logger.info("执行SQL: {}", sql)
    execute(sql, columns, rows)
  }

  protected def update(table: String, columns: Seq[String], rows: Iterator[Row]): Unit = {
    if (CollectionUtils.isEmpty(rows) || CollectionUtils.isEmpty(columns)) {
      logger.warn("字段列为空或数据为空，跳过数据写入")
      return
    }
    val sql = updateSql(table, columns)
    logger.info("执行SQL: {}", sql)
    execute(sql, columns, rows)
  }

  protected def execute(sql: String, columns: Seq[String], rows: Iterator[Row]): Unit = {
    if (CollectionUtils.isEmpty(rows) || CollectionUtils.isEmpty(columns)) {
      logger.warn("字段列为空或数据为空，跳过数据写入")
      return
    }
    val batchArgs = new Iterator[Seq[Any]]() {
      override def hasNext: Boolean = rows.hasNext

      override def next(): Seq[Any] = {
        val row = rows.next()
        columns.map { name =>
          val value: Any = row.getAs(name)
          value
        }
      }
    }
    JdbcUtils.executeBatch(getCon, sql, batchArgs, batchSize)
  }

  protected def insertSql(table: String, columns: Seq[String]): String

  protected def updateSql(table: String, columns: Seq[String]): String

  protected def quoteIdentifier(identifier: String): String = identifier

}
