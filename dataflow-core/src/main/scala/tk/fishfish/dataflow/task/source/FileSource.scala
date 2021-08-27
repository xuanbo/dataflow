package tk.fishfish.dataflow.task.source

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.task.{Argument, Result, Task}
import tk.fishfish.dataflow.util.{JdbcOptions, SparkUtils, StringTemplate, Validation}

class FileSource extends Task {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  private var spark: SparkSession = _

  private val supportedFormats = Set("csv", "json", "parquet", "orc")

  override def name(): String = "FILE_SOURCE"

  override def execute(argument: Argument): Result = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")
    Validation.nonNull(argument.output, "配置 [argument.output] 不能为空")

    var path = argument.input.getString("path")
    val format = argument.input.getString("format")
    var table = argument.output.getString(JdbcOptions.TABLE)
    Validation.nonEmpty(path, "配置 [argument.input.path] 不能为空")
    Validation.nonEmpty(format, "配置 [argument.input.format] 不能为空")
    Validation.nonEmpty(table, "配置 [argument.output.table] 不能为空")

    if (!supportedFormats.contains(format)) {
      throw new IllegalArgumentException(s"不支持的format类型: $format, 支持的类型: ${supportedFormats.mkString(", ")}")
    }

    path = StringTemplate.render(path, argument.context.toMap)
    table = s"${argument.namespace}.$table"
    logger.info(s"输入文件: $path, 格式: $format, 输出表: $table")

    val reader = spark.read
    if ("csv".equals(format)) {
      reader.option("header", "true")
    }

    SparkUtils.setTaskId(spark, argument)

    reader.format(format).load(path)
      .write.mode(SaveMode.Overwrite).saveAsTable(table)

    val df = spark.table(table)
    val count = df.count()
    Result(SparkUtils.takeJSON(df), count)
  }

  override def setSparkSession(spark: SparkSession): Unit = this.spark = spark

}
