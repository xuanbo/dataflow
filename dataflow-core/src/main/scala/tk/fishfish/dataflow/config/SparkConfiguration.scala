package tk.fishfish.dataflow.config

import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.core.env.Environment
import org.springframework.validation.annotation.Validated
import tk.fishfish.dataflow.task.Task
import tk.fishfish.dataflow.util.ServiceLoaderUtils

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
  def tasks(spark: SparkSession, env: Environment): Seq[Task] = {
    logger.info(s"加载自定义组件: ${classOf[Task].getName}, classloader: ${classOf[Task].getClassLoader.getClass.getName}")
    val tasks = ServiceLoaderUtils.load(classOf[Task])
    tasks.foreach { task =>
      task.setEnv(env)
      task.setSparkSession(spark)
    }
    tasks
  }

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
