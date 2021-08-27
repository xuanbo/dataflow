package tk.fishfish.dataflow.task.transformer

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.task.{Argument, Result, Task}
import tk.fishfish.dataflow.util.{JdbcOptions, SparkUtils, StringTemplate, Validation}

class SqlTransformer extends Task {

  private val logger: Logger = LoggerFactory.getLogger(classOf[SqlTransformer])

  private var spark: SparkSession = _

  override def name(): String = "SQL_TRANSFORMER"

  override def execute(argument: Argument): Result = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")
    Validation.nonNull(argument.output, "配置 [argument.output] 不能为空")

    var sql = argument.input.getString(JdbcOptions.SQL)
    var table = argument.output.getString(JdbcOptions.TABLE)
    Validation.nonEmpty(sql, "配置 [argument.input.sql] 不能为空")

    // 变量替换
    sql = StringTemplate.render(sql, argument.context.toMap)
    table = s"${argument.namespace}.$table"
    logger.info(s"查询SQL: $sql, 输出表: $table")

    val df = spark.sql(sql)
    if (StringUtils.isNotBlank(table)) {
      SparkUtils.setTaskId(spark, argument)

      df.write.mode(SaveMode.Overwrite).saveAsTable(table)
    }

    val count = df.count()
    Result(SparkUtils.takeJSON(df), count)
  }

  override def setSparkSession(spark: SparkSession): Unit = this.spark = spark

}
