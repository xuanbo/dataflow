package tk.fishfish.dataflow.config

import org.apache.commons.lang3.BooleanUtils
import org.apache.spark.sql.SparkSession
import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.validation.annotation.Validated
import tk.fishfish.dataflow.dag.{ConcurrentDagExecutor, DagExecutor, DagSparkListener, SimpleDagExecutor}
import tk.fishfish.dataflow.service.{ExecutionService, TaskService}
import tk.fishfish.dataflow.task.Task

import scala.beans.BeanProperty

/**
 * DAG执行器配置
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Configuration
@EnableConfigurationProperties(Array(classOf[DagProperties]))
class DagConfiguration {

  @Bean
  def dagSparkListener(spark: SparkSession): DagSparkListener = {
    val dagSparkListener = new DagSparkListener()
    spark.sparkContext.addSparkListener(dagSparkListener)
    dagSparkListener
  }

  @Bean
  def dagExecutor(spark: SparkSession, taskSeq: Seq[Task],
                  executionService: ExecutionService, taskService: TaskService,
                  properties: DagProperties, sparkListener: DagSparkListener): DagExecutor = {
    val tasks = taskSeq.map(e => (e.name(), e)).toMap
    if (BooleanUtils.toBoolean(properties.concurrent)) {
      new ConcurrentDagExecutor(spark, tasks, executionService, taskService, sparkListener)
    } else {
      new SimpleDagExecutor(spark, tasks, executionService, taskService)
    }
  }

}

@Validated
@ConfigurationProperties(prefix = "dataflow.dag")
class DagProperties {

  @BeanProperty
  var concurrent: Boolean = _

}
