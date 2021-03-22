package tk.fishfish.dataflow.core

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.util.Validation

/**
 * 过滤
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Filter extends Task {

  def filter(argument: Argument): Unit

}

class SqlFilter(val spark: SparkSession) extends Filter {

  private val logger: Logger = LoggerFactory.getLogger(classOf[SqlFilter])

  override def name(): String = "FILTER_SQL"

  override def filter(argument: Argument): Unit = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")
    Validation.nonNull(argument.output, "配置 [argument.output] 不能为空")

    var inTable = argument.input.getString("table")
    val where = argument.input.getString("where")
    val orderBy = argument.input.getString("orderBy")
    val limit = argument.input.getString("limit")
    Validation.nonEmpty(inTable, "配置 [argument.input.table] 不能为空")

    var outTable = argument.output.getString("table")
    Validation.nonEmpty(outTable, "配置 [argument.output.table] 不能为空")

    inTable = s"${argument.namespace}_$inTable"
    outTable = s"${argument.namespace}_$outTable"
    var sql = s"SELECT * FROM $inTable"
    if (StringUtils.isNotEmpty(where)) {
      sql = sql + s" WHERE $where"
    }
    if (StringUtils.isNotEmpty(orderBy)) {
      sql = sql + s" ORDER BY $orderBy"
    }
    if (StringUtils.isNotEmpty(limit)) {
      sql = sql + s" LIMIT $limit"
    }
    logger.info(s"过滤SQL: $sql, 输出表: $outTable")
    spark.sql(sql).createOrReplaceTempView(outTable)

    // 缓存表
    spark.sqlContext.cacheTable(outTable)
    spark.sqlContext.table(outTable).count()

    argument.tables = Seq(inTable, outTable)
  }

}
