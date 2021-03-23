package tk.fishfish.dataflow.config

import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.validation.annotation.Validated
import tk.fishfish.dataflow.core.Task
import tk.fishfish.dataflow.dag.{DagExecutor, DefaultDagExecutor}
import tk.fishfish.dataflow.service.{ExecutionService, TaskService}

import java.util.{Collections, ServiceLoader}
import javax.validation.constraints.NotBlank
import scala.beans.BeanProperty
import scala.collection.mutable

/**
 * Spark配置
 *
 * @author 奔波儿灞
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(Array(classOf[SparkProperties]))
class SparkConfiguration {

  private val logger: Logger = LoggerFactory.getLogger(classOf[SparkConfiguration])

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
  def tasks(spark: SparkSession): Seq[Task] = {
    var seq = mutable.Seq[Task]()
    logger.info(s"加载自定义组件: ${classOf[Task].getName}, classloader: ${classOf[Task].getClassLoader.getClass.getName}")
    import scala.collection.JavaConversions.asScalaIterator
    ServiceLoader.load(classOf[Task], classOf[Task].getClassLoader).iterator().foreach { e =>
      logger.info(s"自定义组件: ${e.name()}, class: ${e.getClass.getName}")
      e.setSparkSession(spark)
      seq = seq :+ e
    }
    seq
  }

  @Bean
  def dagExecutor(spark: SparkSession, tasks: Seq[Task], executionService: ExecutionService, taskService: TaskService): DagExecutor =
    new DefaultDagExecutor(spark, tasks.map(e => (e.name(), e)).toMap, executionService, taskService)

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
