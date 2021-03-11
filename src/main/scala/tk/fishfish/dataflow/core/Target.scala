package tk.fishfish.dataflow.core

import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import tk.fishfish.dataflow.util.Validation

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

class SqlTarget(val spark: SparkSession) extends Target {

  override def taskType(): String = "SQL_TARGET"

  override def write(df: DataFrame, conf: Conf): Unit = {
    Validation.notNull(conf.jdbc, "配置 [conf.jdbc] 不能为空")
    Validation.notEmpty(conf.jdbc.url, "配置 [conf.jdbc.url] 不能为空")
    Validation.notEmpty(conf.jdbc.table, "配置 [conf.jdbc.table] 不能为空")
    df.write.format("jdbc")
      .option(JDBCOptions.JDBC_URL, conf.jdbc.url)
      .option("user", conf.jdbc.user)
      .option("password", conf.jdbc.password)
      .option(JDBCOptions.JDBC_TABLE_NAME, conf.jdbc.table)
      .mode(SaveMode.Append)
      .save()
  }

}

