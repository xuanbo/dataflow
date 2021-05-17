package tk.fishfish.dataflow.core

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.exception.DagException
import tk.fishfish.dataflow.util.{StringTemplate, Validation}

/**
 * 源端
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Source extends Task {

  def read(argument: Argument): Unit

}

class SqlSource extends Source {

  private val logger: Logger = LoggerFactory.getLogger(classOf[SqlSource])

  private var spark: SparkSession = _

  override def name(): String = "SOURCE_SQL"

  override def read(argument: Argument): Unit = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")
    Validation.nonNull(argument.output, "配置 [argument.output] 不能为空")

    val url = argument.input.getString("url")
    val user = argument.input.getString("user")
    val password = argument.input.getString("password")
    var sql = argument.input.getString("sql")
    Validation.nonEmpty(url, "配置 [argument.input.url] 不能为空")
    Validation.nonEmpty(sql, "配置 [argument.input.sql] 不能为空")

    var table = argument.output.getString("table")
    Validation.nonEmpty(table, "配置 [argument.output.table] 不能为空")

    // 变量替换
    sql = StringTemplate.render(sql, argument.context.toMap)

    table = s"${argument.namespace}_$table"
    logger.info(s"查询SQL: $sql, 输出表: $table")

    if (StringUtils.startsWith(url, "jdbc:iotdb://")) {
      spark.read.format("org.apache.iotdb.spark.db")
        .option(JDBCOptions.JDBC_URL, url)
        .option("user", user)
        .option("password", password)
        .option("sql", sql)
        .load()
        .createOrReplaceTempView(table)
    } else {
      spark.read.format("jdbc")
        // 防止集群下，找不到驱动
        .option(JDBCOptions.JDBC_DRIVER_CLASS, determineDriverClass(url))
        .option(JDBCOptions.JDBC_URL, url)
        .option("user", user)
        .option("password", password)
        .option(JDBCOptions.JDBC_QUERY_STRING, sql)
        .option(JDBCOptions.JDBC_NUM_PARTITIONS, 4)
        .option(JDBCOptions.JDBC_BATCH_FETCH_SIZE, 1024)
        .load()
        .createOrReplaceTempView(table)
    }

    // 缓存表
    spark.sqlContext.cacheTable(table)
    spark.sqlContext.table(table).count()
  }

  private def determineDriverClass(url: String): String = {
    if (url.startsWith("jdbc:mysql://")) {
      classOf[com.mysql.cj.jdbc.Driver].getName
    } else {
      throw new DagException(s"不支持的驱动: $url");
    }
  }

  override def setSparkSession(spark: SparkSession): Unit = this.spark = spark

}
