package tk.fishfish.dataflow.task.source

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.database.source.SourceFactory
import tk.fishfish.dataflow.task.{Argument, Result, Task}
import tk.fishfish.dataflow.util.{JdbcOptions, Properties, ServiceLoaderUtils, SparkUtils, StringTemplate, Validation}

class DatabaseSource extends Task {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DatabaseSource])

  private val sourceFactories: Seq[SourceFactory] = ServiceLoaderUtils.load(classOf[SourceFactory])

  private var spark: SparkSession = _

  override def name(): String = "DATABASE_SOURCE"

  override def execute(argument: Argument): Result = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")
    Validation.nonNull(argument.output, "配置 [argument.output] 不能为空")

    val url = argument.input.getString(JdbcOptions.URL)
    var table = argument.output.getString(JdbcOptions.TABLE)
    Validation.nonEmpty(url, "配置 [argument.input.url] 不能为空")
    Validation.nonEmpty(table, "配置 [argument.output.table] 不能为空")

    // SQL变量替换
    var sql = argument.input.getString("sql")
    if (StringUtils.isNotBlank(sql)) {
      sql = StringTemplate.render(sql, argument.context.toMap)
      argument.input.put("sql", sql)
    }

    table = s"${argument.namespace}.$table"
    logger.info(s"输出表: $table")

    val props = new Properties()
    import scala.collection.JavaConversions.mapAsScalaMap
    argument.input.foreach(e => props.option(e._1, e._2))

    sourceFactories.find(_.accept(url)) match {
      case Some(factory) => {
        SparkUtils.setTaskId(spark, argument)

        val source = factory.create(props)
        source.read(spark)
          .write.mode(SaveMode.Overwrite).saveAsTable(table)

        val df = spark.table(table)
        val count = df.count()
        Result(SparkUtils.takeJSON(df), count)
      }
      case None => throw new UnsupportedOperationException(s"不支持的数据源: $url")
    }
  }

  override def setSparkSession(spark: SparkSession): Unit = this.spark = spark

}
