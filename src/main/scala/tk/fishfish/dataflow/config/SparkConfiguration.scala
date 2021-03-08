package tk.fishfish.dataflow.config

import org.apache.spark.sql.SparkSession
import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.validation.annotation.Validated
import tk.fishfish.dataflow.core._
import tk.fishfish.dataflow.dag.{DagExecutor, DefaultDagExecutor}
import tk.fishfish.dataflow.service.{ExecutionService, FlowService}

import java.util.Collections
import javax.validation.constraints.NotBlank
import scala.beans.BeanProperty

/**
 * Spark配置
 *
 * @author 奔波儿灞
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(Array(classOf[SparkProperties]))
class SparkConfiguration {

  @Bean(destroyMethod = "stop")
  def sparkSession(properties: SparkProperties): SparkSession = {
    val builder = SparkSession.builder
      .appName(properties.appName)
      .master(properties.master)

    val config = properties.config
    import scala.collection.JavaConversions.mapAsScalaMap
    for ((k, v) <- config) {
      builder.config(k, v)
    }

    builder.getOrCreate()
  }

  @Bean
  def tasks(spark: SparkSession): Seq[Task] = Seq(
    new SqlSource(spark), new IoTSource(spark),
    new DefaultTransformer(spark),
    new SqlFilter(spark),
    new LogTarget(spark), new SqlTarget(spark)
  )

  @Bean
  def dagExecutor(tasks: Seq[Task], executionService: ExecutionService, flowService: FlowService): DagExecutor =
    new DefaultDagExecutor(tasks, executionService, flowService)
}

@Validated
@ConfigurationProperties(prefix = "spark")
class SparkProperties {

  @NotBlank
  @BeanProperty
  var appName: String = _

  @NotBlank
  @BeanProperty
  var master: String = _

  @BeanProperty
  var config: java.util.Map[String, String] = Collections.emptyMap()

}
