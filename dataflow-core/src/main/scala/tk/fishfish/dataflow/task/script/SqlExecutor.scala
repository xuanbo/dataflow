package tk.fishfish.dataflow.task.script

import com.google.common.base.Splitter
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.database.sql.ExecutorFactory
import tk.fishfish.dataflow.task.{Argument, Result, Task}
import tk.fishfish.dataflow.util.{JdbcOptions, Properties, ServiceLoaderUtils, StringTemplate, Validation}

class SqlExecutor extends Task {

  private val logger: Logger = LoggerFactory.getLogger(classOf[SqlExecutor])

  private val executorFactories: Seq[ExecutorFactory] = ServiceLoaderUtils.load(classOf[ExecutorFactory])

  private var spark: SparkSession = _

  override def name(): String = "SQL_EXECUTOR"

  def execute(argument: Argument): Result = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")
    Validation.nonNull(argument.output, "配置 [argument.output] 不能为空")

    val url = argument.input.getString(JdbcOptions.URL)
    val user = argument.input.getString(JdbcOptions.USER)
    val password = argument.input.getString(JdbcOptions.PASSWORD)
    var sql = argument.input.getString(JdbcOptions.SQL)
    Validation.nonEmpty(url, "配置 [argument.input.url] 不能为空")
    Validation.nonEmpty(sql, "配置 [argument.input.sql] 不能为空")

    sql = StringTemplate.render(sql, argument.context.toMap)
    import scala.collection.JavaConversions.asScalaBuffer
    val sqlList: Seq[String] = Splitter.on(";").trimResults().omitEmptyStrings().splitToList(sql)
    logger.info(s"执行SQL: [$sql], JDBC url: $url")

    val props = new Properties()
      .option(JdbcOptions.URL, url)
      .option(JdbcOptions.USER, user)
      .option(JdbcOptions.PASSWORD, password)

    executorFactories.find(_.accept(url)) match {
      case Some(factory) => {
        factory.create(props).execute(sqlList: _*)

        Result.empty()
      }
      case None => throw new UnsupportedOperationException(s"不支持的数据源: $url")
    }
  }

  override def setSparkSession(spark: SparkSession): Unit = this.spark = spark

}
