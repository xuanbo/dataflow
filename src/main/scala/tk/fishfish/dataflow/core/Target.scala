package tk.fishfish.dataflow.core

import org.apache.spark.sql.{DataFrame, SparkSession}
import tk.fishfish.dataflow.database.DataHubFactory
import tk.fishfish.dataflow.entity.enums.JdbcProperty
import tk.fishfish.dataflow.exception.FlowException
import tk.fishfish.dataflow.service.DatabaseService
import tk.fishfish.dataflow.util.{Properties, StringUtils, Validation}

/**
 * 目标
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Target extends Task {

  def write(df: DataFrame, conf: Conf): Unit

}

class LogTarget(val spark: SparkSession) extends Target {

  override def taskType(): String = "LOG_TARGET"

  override def write(df: DataFrame, conf: Conf): Unit = df.show()

}

class SqlTarget(val spark: SparkSession, val databaseService: DatabaseService) extends Target {

  override def taskType(): String = "SQL_TARGET"

  override def write(df: DataFrame, conf: Conf): Unit = {
    Validation.notNull(conf.jdbc, "配置 [conf.jdbc] 不能为空")
    Validation.notEmpty(conf.jdbc.table, "配置 [conf.jdbc.table] 不能为空")
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
    val table = conf.jdbc.table
    val columns = df.schema.map(_.name)
    val props = new Properties()
      .option(JdbcProperty.URL.key(), conf.jdbc.url)
      .option(JdbcProperty.USER.key(), conf.jdbc.user)
      .option(JdbcProperty.PASSWORD.key(), conf.jdbc.password)
    df.foreachPartition(rows => DataHubFactory.create(props).insert(table, columns, rows))
  }

}

