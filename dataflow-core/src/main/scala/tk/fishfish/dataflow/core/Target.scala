package tk.fishfish.dataflow.core

import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.database.DataHubFactory
import tk.fishfish.dataflow.entity.enums.JdbcProperty
import tk.fishfish.dataflow.util.{Properties, Validation}

/**
 * 目标
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Target extends Task {

  def write(argument: Argument): Unit

}

class LogTarget extends Target {

  private var spark: SparkSession = _

  override def name(): String = "TARGET_LOG"

  override def write(argument: Argument): Unit = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")

    var table = argument.input.getString("table")
    Validation.nonEmpty(table, "配置 [argument.input.table] 不能为空")

    table = s"${argument.namespace}_$table"
    spark.table(table).show()

    argument.tables = Seq(table)
  }

  override def setSparkSession(spark: SparkSession): Unit = this.spark = spark

}

class SqlTarget extends Target {

  private val logger: Logger = LoggerFactory.getLogger(classOf[SqlTarget])

  private var spark: SparkSession = _

  override def name(): String = "TARGET_SQL"

  override def write(argument: Argument): Unit = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")
    Validation.nonNull(argument.output, "配置 [argument.output] 不能为空")

    var inTable = argument.input.getString("table")
    Validation.nonEmpty(inTable, "配置 [argument.input.table] 不能为空")

    val url = argument.output.getString("url")
    val user = argument.output.getString("user")
    val password = argument.output.getString("password")
    val outTable = argument.output.getString("table")
    val partition = argument.output.getInt("partition", 4)
    Validation.nonEmpty(url, "配置 [argument.output.url] 不能为空")
    Validation.nonEmpty(outTable, "配置 [argument.output.table] 不能为空")

    inTable = s"${argument.namespace}_$inTable"
    logger.info(s"读入表: $inTable, 写入表: $outTable")

    val props = new Properties()
      .option(JdbcProperty.URL.key(), url)
      .option(JdbcProperty.USER.key(), user)
      .option(JdbcProperty.PASSWORD.key(), password)
    val df = spark.table(inTable)
    val columns = df.schema.map(_.name)
    df.repartition(partition).foreachPartition(rows => DataHubFactory.create(props).insert(outTable, columns, rows))

    argument.tables = Seq(inTable)
  }

  override def setSparkSession(spark: SparkSession): Unit = this.spark = spark

}
