package tk.fishfish.dataflow.task.sink

import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.database.sink.SinkFactory
import tk.fishfish.dataflow.task.{Argument, Result, Task}
import tk.fishfish.dataflow.util.{JdbcOptions, Properties, ServiceLoaderUtils, SparkUtils, Validation}

class DatabaseSink extends Task {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DatabaseSink])

  private val sinkFactories: Seq[SinkFactory] = ServiceLoaderUtils.load(classOf[SinkFactory])

  private var spark: SparkSession = _

  override def name(): String = "DATABASE_SINK"

  override def execute(argument: Argument): Result = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")
    Validation.nonNull(argument.output, "配置 [argument.output] 不能为空")

    var inTable = argument.input.getString(JdbcOptions.TABLE)
    val url = argument.output.getString(JdbcOptions.URL)
    val outTable = argument.output.getString(JdbcOptions.TABLE)
    val numPartitions = argument.output.getInt(JdbcOptions.NUM_PARTITIONS, 0)
    Validation.nonEmpty(inTable, "配置 [argument.input.table] 不能为空")
    Validation.nonEmpty(url, "配置 [argument.output.url] 不能为空")
    Validation.nonEmpty(outTable, "配置 [argument.output.table] 不能为空")

    inTable = s"${argument.namespace}.$inTable"
    logger.info(s"读入表: $inTable, 写入表: $outTable")

    val props = new Properties()
    import scala.collection.JavaConversions.mapAsScalaMap
    argument.output.foreach(e => props.option(e._1, e._2))

    sinkFactories.find(_.accept(url)) match {
      case Some(factory) => {
        val df = spark.table(inTable)
        val partitionedDf = if (numPartitions > 0) {
          df.repartition(numPartitions)
        } else {
          df
        }

        SparkUtils.setTaskId(spark, argument)

        factory.create(props).write(partitionedDf)

        val count = df.count()
        Result(SparkUtils.takeJSON(df), count)
      }
      case None => throw new UnsupportedOperationException(s"不支持的数据源: $url")
    }
  }

  override def setSparkSession(spark: SparkSession): Unit = this.spark = spark

}
