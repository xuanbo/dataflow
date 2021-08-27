package tk.fishfish.dataflow.util

import org.apache.spark.sql.catalyst.analysis.UnresolvedRelation
import org.apache.spark.sql.{DataFrame, SparkSession}
import tk.fishfish.dataflow.task.Argument
import tk.fishfish.json.util.JSON

/**
 * Spark工具类
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object SparkUtils {

  def takeJSON(df: DataFrame, limit: Int = 10): String = {
    val list = df.take(limit).map { row =>
      row.schema.map { field =>
        (field.name, row.getAs(field.name))
      }.toMap
    }
    JSON.write(list)
  }

  def createDatabase(spark: SparkSession, db: String): Unit = spark.sql(s"CREATE DATABASE IF NOT EXISTS $db")

  def dropDatabase(spark: SparkSession, db: String): Unit = spark.sql(s"DROP DATABASE IF EXISTS $db CASCADE")

  def parseTables(spark: SparkSession, query: String): Array[String] = {
    val logicalPlan = spark.sessionState.sqlParser.parsePlan(query)
    logicalPlan.collect { case r: UnresolvedRelation => r.tableName }.toArray
  }

  def setTaskId(spark: SparkSession, argument: Argument): Unit = {
    argument.context.get("taskId").foreach { taskId =>
      spark.sparkContext.setLocalProperty("taskId", taskId.toString)
    }
  }

}
