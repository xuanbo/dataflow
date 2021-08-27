package tk.fishfish.dataflow.task.sink

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.task.{Argument, Result, Task}
import tk.fishfish.dataflow.util.{JdbcOptions, SparkUtils, StringTemplate, Validation}

class FileSink extends Task {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private var spark: SparkSession = _

  private val supportedFormats = Set("csv", "json", "parquet", "orc")

  override def name(): String = "FILE_SINK"

  override def execute(argument: Argument): Result = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")
    Validation.nonNull(argument.output, "配置 [argument.output] 不能为空")

    var table = argument.input.getString(JdbcOptions.TABLE)
    var path = argument.output.getString("path")
    val format = argument.output.getString("format")
    Validation.nonEmpty(table, "配置 [argument.input.table] 不能为空")
    Validation.nonEmpty(path, "配置 [argument.output.path] 不能为空")
    Validation.nonEmpty(format, "配置 [argument.output.format] 不能为空")

    if (!supportedFormats.contains(format)) {
      throw new IllegalArgumentException(s"不支持的format类型: $format, 支持的类型: ${supportedFormats.mkString(", ")}")
    }

    table = s"${argument.namespace}.$table"
    path = StringTemplate.render(path, argument.context.toMap)
    logger.info(s"读入表: $table, 输出文件: $path, 格式: $format")

    val df = spark.table(table)
    val writer = df.write
    if ("csv".equals(format)) {
      writer.option("header", "true")
    }

    SparkUtils.setTaskId(spark, argument)

    writer.mode(SaveMode.Overwrite).format(format).save(path)

    val count = df.count()
    Result(SparkUtils.takeJSON(df), count)
  }

  override def setSparkSession(spark: SparkSession): Unit = this.spark = spark

}