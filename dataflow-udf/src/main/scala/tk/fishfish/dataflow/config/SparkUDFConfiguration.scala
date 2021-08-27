package tk.fishfish.dataflow.config

import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, Configuration}
import tk.fishfish.dataflow.udf.UDF
import tk.fishfish.dataflow.util.ServiceLoaderUtils

/**
 * Spark SQL UDF配置
 *
 * @author 奔波儿灞
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(Array(classOf[SparkProperties]))
class SparkUDFConfiguration {

  private val logger: Logger = LoggerFactory.getLogger(classOf[SparkConfiguration])

  @Bean
  def udf(spark: SparkSession): Seq[UDF] = {
    val udfSeq = ServiceLoaderUtils.load(classOf[UDF])
    udfSeq.foreach { e =>
      logger.info("注册UDF: {}", e.name())
      spark.udf.register(e.name(), e.udf)
    }
    udfSeq
  }

}
