package tk.fishfish.dataflow.core

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.util.Validation

/**
 * 转换
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Transformer extends Task {

  def transform(argument: Argument): Unit

}

class SqlTransformer extends Transformer {

  private val logger: Logger = LoggerFactory.getLogger(classOf[SqlTransformer])

  private var spark: SparkSession = _

  override def name(): String = "TRANSFORMER_SQL"

  override def transform(argument: Argument): Unit = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")
    Validation.nonNull(argument.output, "配置 [argument.output] 不能为空")

    val select = argument.input.getString("select")
    var inTable = argument.input.getString("table")
    val groupBy = argument.input.getString("groupBy")
    Validation.nonEmpty(select, "配置 [argument.input.select] 不能为空")
    Validation.nonEmpty(inTable, "配置 [argument.input.table] 不能为空")

    var outTable = argument.output.getString("table")
    Validation.nonEmpty(outTable, "配置 [argument.output.table] 不能为空")

    inTable = s"${argument.namespace}_$inTable"
    outTable = s"${argument.namespace}_$outTable"
    var sql = s"SELECT $select FROM $inTable"
    if (StringUtils.isNotEmpty(groupBy)) {
      sql = sql + s" GROUP BY $groupBy"
    }
    logger.info(s"转换SQL: $sql, 输出表: $outTable")
    spark.sql(sql).createOrReplaceTempView(outTable)

    // 缓存表
    spark.sqlContext.cacheTable(outTable)
    spark.sqlContext.table(outTable).count()

    argument.tables = Seq(inTable, outTable)
  }

  override def setSparkSession(spark: SparkSession): Unit = this.spark = spark

}

class SqlJoinTransformer extends Transformer {

  private val logger: Logger = LoggerFactory.getLogger(classOf[SqlJoinTransformer])

  private var spark: SparkSession = _

  override def name(): String = "TRANSFORMER_SQL_JOIN"

  override def transform(argument: Argument): Unit = {
    Validation.nonNull(argument.input, "配置 [argument.input] 不能为空")
    Validation.nonNull(argument.output, "配置 [argument.output] 不能为空")

    val select = argument.input.getString("select")
    var inTable = argument.input.getString("table")
    val inTableAlias = argument.input.getString("tableAlias")
    val joins = argument.input.getSeq("joins")
    val where = argument.input.getString("where")
    val groupBy = argument.input.getString("groupBy")
    val orderBy = argument.input.getString("orderBy")
    val limit = argument.input.getString("limit")
    Validation.nonEmpty(select, "配置 [argument.input.select] 不能为空")
    Validation.nonEmpty(inTable, "配置 [argument.input.table] 不能为空")
    Validation.nonEmpty(inTableAlias, "配置 [argument.input.tableAlias] 不能为空")
    Validation.nonEmpty(joins, "配置 [argument.input.joins] 不能为空")

    var outTable = argument.output.getString("table")
    Validation.nonEmpty(outTable, "配置 [argument.output.table] 不能为空")

    inTable = s"${argument.namespace}_$inTable"
    outTable = s"${argument.namespace}_$outTable"

    val join = joins.map { e =>
      val table = e.getString("table")
      val tableAlias = e.getString("tableAlias")
      val on = e.getString("on")

      Validation.nonEmpty(table, "配置 [argument.input.joins[*].table] 不能为空")
      Validation.nonEmpty(tableAlias, "配置 [argument.input.joins[*].tableAlias] 不能为空")
      Validation.nonEmpty(on, "配置 [argument.input.joins[*].on] 不能为空")

      s"LEFT JOIN ${argument.namespace}_$table AS $tableAlias ON $on"
    }.mkString(" ")
    var sql = s"SELECT $select FROM $inTable AS $inTableAlias $join"
    if (StringUtils.isNotEmpty(where)) {
      sql = sql + s" WHERE $where"
    }
    if (StringUtils.isNotEmpty(groupBy)) {
      sql = sql + s" GROUP BY $groupBy"
    }
    if (StringUtils.isNotEmpty(orderBy)) {
      sql = sql + s" ORDER BY $orderBy"
    }
    if (StringUtils.isNotEmpty(limit)) {
      sql = sql + s" LIMIT $limit"
    }

    logger.info(s"JOIN SQL: $sql, 输出表: $outTable")
    spark.sql(sql).createOrReplaceTempView(outTable)

    // 缓存表
    spark.sqlContext.cacheTable(outTable)
    spark.sqlContext.table(outTable).count()

    argument.tables = Seq(inTable, outTable)
  }

  override def setSparkSession(spark: SparkSession): Unit = this.spark = spark

}
