package tk.fishfish.dataflow.core

import org.apache.spark.sql.SparkSession

/**
 * 任务
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Task extends Serializable {

  /**
   * 任务名称
   *
   * @return 名称
   */
  def name(): String

  /**
   * 回掉SparkSession
   *
   * @param spark SparkSession
   */
  def setSparkSession(spark: SparkSession): Unit

}

