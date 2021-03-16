package tk.fishfish.dataflow.core

import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.exception.FlowException
import tk.fishfish.dataflow.service.DatabaseService
import tk.fishfish.dataflow.util.{StringUtils, Validation}

/**
 * 源端
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Source extends Task {

  def read(conf: Conf): DataFrame

}

class SqlSource(val spark: SparkSession, val databaseService: DatabaseService) extends Source {

  private val logger: Logger = LoggerFactory.getLogger(classOf[SqlSource])

  override def taskType(): String = "SQL_SOURCE"

  override def read(conf: Conf): DataFrame = {
    Validation.notNull(conf.jdbc, "配置 [conf.jdbc] 不能为空")
    if (StringUtils.isNotEmpty(conf.jdbc.id)) {
      val database = databaseService.findById(conf.jdbc.id)
      if (database == null) {
        throw new FlowException(s"源端[${taskType()}]绑定的数据源ID不存在: ${conf.jdbc.id}")
      }
      conf.jdbc.url = database.getUrl
      conf.jdbc.user = database.getUser
      conf.jdbc.password = database.getPassword
    }
    Validation.notEmpty(conf.jdbc.url, "配置 [conf.jdbc.url] 不能为空")
    var sql = ""
    if (StringUtils.isNotEmpty(conf.jdbc.sql)) {
      // 自定义SQL
      sql = conf.jdbc.sql
    } else {
      // 查询表
      Validation.notEmpty(conf.jdbc.table, "配置 [conf.jdbc.table] 不能为空")
      Validation.notEmpty(conf.columns, "配置 [conf.columns] 不能为空")
      val columns = conf.columns.map(_.name).mkString(", ")
      sql = s"SELECT $columns FROM ${conf.jdbc.table}"
    }
    logger.info("查询SQL: {}", sql)
    spark.read.format("jdbc")
      .option(JDBCOptions.JDBC_URL, conf.jdbc.url)
      .option("user", conf.jdbc.user)
      .option("password", conf.jdbc.password)
      .option(JDBCOptions.JDBC_QUERY_STRING, sql)
      .option(JDBCOptions.JDBC_NUM_PARTITIONS, 10)
      .load()
  }

}

class IoTSource(val spark: SparkSession, val databaseService: DatabaseService) extends Source {

  private val logger: Logger = LoggerFactory.getLogger(classOf[IoTSource])

  override def taskType(): String = "IOT_SOURCE"

  override def read(conf: Conf): DataFrame = {
    Validation.notNull(conf.jdbc, "配置 [conf.jdbc] 不能为空")
    if (StringUtils.isNotEmpty(conf.jdbc.id)) {
      val database = databaseService.findById(conf.jdbc.id)
      if (database == null) {
        throw new FlowException(s"源端[${taskType()}]绑定的数据源ID不存在: ${conf.jdbc.id}")
      }
      conf.jdbc.url = database.getUrl
      conf.jdbc.user = database.getUser
      conf.jdbc.password = database.getPassword
    }
    Validation.notEmpty(conf.jdbc.url, "配置 [conf.jdbc.url] 不能为空")
    Validation.notEmpty(conf.columns, "配置 [conf.columns] 不能为空")
    val columns = conf.columns.map(_.name)
    var sql = ""
    if (StringUtils.isNotEmpty(conf.jdbc.sql)) {
      // 自定义SQL
      sql = conf.jdbc.sql
    } else {
      // 查询表
      Validation.notEmpty(conf.jdbc.table, "配置 [conf.jdbc.table] 不能为空")
      sql = s"SELECT ${columns.mkString(", ")} FROM ${conf.jdbc.table}"
    }
    logger.info("查询SQL: {}", sql)
    spark.read.format("org.apache.iotdb.spark.db")
      .option(JDBCOptions.JDBC_URL, conf.jdbc.url)
      .option("user", conf.jdbc.user)
      .option("password", conf.jdbc.password)
      .option("sql", sql)
      .load()
      .toDF(columns: _*)
  }

}
