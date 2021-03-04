package tk.fishfish.dataflow.core

import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.exception.FlowException

/**
 * 源端
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Source extends Task {

  def supportNext(): Seq[Class[_]] = Seq(classOf[Transformer], classOf[Filter], classOf[Target])

  def read(conf: Conf): DataFrame

}

class SqlSource(val spark: SparkSession) extends Source {

  private val logger: Logger = LoggerFactory.getLogger(classOf[SqlSource])

  override def taskType(): String = "SQL_SOURCE"

  override def read(conf: Conf): DataFrame = {
    if (conf.jdbc == null) {
      throw new FlowException("配置[conf.jdbc]不能为空")
    }
    if (conf.columns == null || conf.columns.isEmpty) {
      throw new FlowException("配置[conf.columns]不能为空")
    }
    val columns = conf.columns.map(_.name).mkString(", ")
    val sql = s"SELECT $columns FROM ${conf.jdbc.table}"
    logger.info("查询SQL: {}", sql)
    spark.read.format("jdbc")
      .option("driver", conf.jdbc.driver)
      .option(JDBCOptions.JDBC_URL, conf.jdbc.url)
      .option("user", conf.jdbc.user)
      .option("password", conf.jdbc.password)
      .option(JDBCOptions.JDBC_QUERY_STRING, sql)
      .option(JDBCOptions.JDBC_NUM_PARTITIONS, 10)
      .load()
  }

}

class IoTSource(val spark: SparkSession) extends Source {

  private val logger: Logger = LoggerFactory.getLogger(classOf[IoTSource])

  override def taskType(): String = "IOT_SOURCE"

  override def read(conf: Conf): DataFrame = {
    if (conf.jdbc == null) {
      throw new FlowException("配置[conf.jdbc]不能为空")
    }
    if (conf.columns == null || conf.columns.isEmpty) {
      throw new FlowException("配置[conf.columns]不能为空")
    }
    val columns = conf.columns.map(_.name).mkString(", ")
    val sql = s"SELECT $columns FROM ${conf.jdbc.table}"
    logger.info("查询SQL: {}", sql)
    spark.read.format("org.apache.iotdb.spark.db")
      .option(JDBCOptions.JDBC_URL, conf.jdbc.url)
      .option("user", conf.jdbc.user)
      .option("password", conf.jdbc.password)
      .option("sql", sql)
      .load()
  }

}
