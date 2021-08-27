package tk.fishfish.dataflow.database.source

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.util.{JdbcOptions, JdbcUtils, Properties, Validation}

import java.sql.{Connection, ResultSet}

/**
 * 关系型数据库源端
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
abstract class RdbmsSource(val props: Properties) extends Source {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  protected val url: String = {
    val url = props.getString(JdbcOptions.URL)
    Validation.nonEmpty(url, "jdbc url参数不能为空")
    url
  }
  protected val user: String = props.getString(JdbcOptions.USER)
  protected val password: String = props.getString(JdbcOptions.PASSWORD)
  protected val fetchSize: Int = props.getInt(JdbcOptions.FETCH_SIZE, 4096)

  override def read(spark: SparkSession): DataFrame = {
    var table = props.getString(JdbcOptions.TABLE)
    if (StringUtils.isBlank(table)) {
      val sql = props.getString(JdbcOptions.SQL)
      if (StringUtils.isBlank(sql)) {
        throw new IllegalArgumentException("配置 [argument.input.table] [argument.input.sql] 不能同时为空")
      }
      table = s"($sql) TMP"
    }
    logger.info(s"查询表: $table, JDBC url: $url")

    val partitionColumn = props.getString(JdbcOptions.PARTITION_COLUMN)
    val numPartitions = props.getInt(JdbcOptions.NUM_PARTITIONS, 4)
    var bounds: (Long, Long) = null
    if (StringUtils.isNotBlank(partitionColumn)) {
      logger.info(s"使用多分区进行数据读取优化，${JdbcOptions.PARTITION_COLUMN}: $partitionColumn," +
        s" ${JdbcOptions.NUM_PARTITIONS}: $numPartitions")
      bounds = obtainBounds(url, user, password, table, partitionColumn)
      logger.info(s"上下边界: {}", bounds)
    }

    val reader = spark.read.format("jdbc")
      .option(JDBCOptions.JDBC_DRIVER_CLASS, driverClass())
      .option(JDBCOptions.JDBC_URL, url)
      .option(JdbcOptions.USER, user)
      .option(JdbcOptions.PASSWORD, password)
      .option(JDBCOptions.JDBC_TABLE_NAME, table)
      .option(JDBCOptions.JDBC_BATCH_FETCH_SIZE, fetchSize)

    if (bounds != null) {
      reader.option(JDBCOptions.JDBC_PARTITION_COLUMN, partitionColumn)
        .option(JDBCOptions.JDBC_LOWER_BOUND, bounds._1)
        .option(JDBCOptions.JDBC_UPPER_BOUND, bounds._2)
        .option(JDBCOptions.JDBC_NUM_PARTITIONS, numPartitions)
    }

    reader.load()
  }

  protected def obtainBounds(url: String, user: String, password: String,
                             table: String, partitionColumn: String): (Long, Long) = {
    val sql =
      s"""
         |SELECT MIN(${quoteIdentifier(partitionColumn)}), MAX(${quoteIdentifier(partitionColumn)}) FROM $table
         |""".stripMargin.replaceAll("\n", "")
    logger.info("查询边界SQL: {}", sql)

    def connectionProvider: Connection = JdbcUtils.getCon(url, user, password)

    def resultSetExtractor(rs: ResultSet): (Long, Long) = {
      if (rs.next()) {
        try {
          val lowerBound = rs.getLong(1)
          val upperBound = rs.getLong(2)
          (lowerBound, upperBound)
        } catch {
          case e: Exception => throw new IllegalArgumentException(s"获取分区字段[$partitionColumn]上下边界错误: ${e.getMessage}", e)
        }
      } else {
        null
      }
    }

    JdbcUtils.query(connectionProvider, sql, resultSetExtractor)
  }

  protected def driverClass(): String

  protected def quoteIdentifier(identifier: String): String = identifier

}
