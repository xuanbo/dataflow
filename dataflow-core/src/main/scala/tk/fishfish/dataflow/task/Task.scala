package tk.fishfish.dataflow.task

import org.apache.spark.sql.SparkSession
import org.springframework.core.env.Environment

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
   * 回调SparkSession
   *
   * @param spark SparkSession
   */
  def setSparkSession(spark: SparkSession): Unit

  /**
   * 回调Environment
   *
   * @param env Environment
   */
  def setEnv(env: Environment): Unit = {}

  /**
   * 执行
   *
   * @param argument 参数
   * @return 结果
   */
  def execute(argument: Argument): Result

}
