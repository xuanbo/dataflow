package tk.fishfish.dataflow.util

import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

/**
 * Spark工具类
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object SparkUtils {

  private val logger: Logger = LoggerFactory.getLogger(SparkUtils.getClass)

  def cleanup(spark: SparkSession, namespace: String): Unit = {
    spark.sql(s"SHOW TABLES")
      .collect()
      .map(_.get(1).asInstanceOf[String])
      .filter(_.startsWith(s"${namespace}_"))
      .foreach { name =>
        logger.info("清理临时表: {}", name)
        try {
          spark.sql(s"DROP TABLE IF EXISTS $name")
        } catch {
          case e: Exception => logger.warn(s"清理临时表 ${name} 失败", e)
        }
      }
  }

}
