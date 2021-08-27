package tk.fishfish.dataflow.dag

import org.apache.spark.scheduler.{SparkListener, SparkListenerJobEnd, SparkListenerJobStart}
import org.slf4j.{Logger, LoggerFactory}

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions.mapAsScalaConcurrentMap
import scala.collection.concurrent

/**
 * 任务监听
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class DagSparkListener extends SparkListener {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DagSparkListener])

  private val map: concurrent.Map[String, Int] = new ConcurrentHashMap[String, Int]()

  override def onJobStart(jobStart: SparkListenerJobStart): Unit = {
    val taskId = jobStart.properties.getProperty("taskId")
    if (taskId != null) {
      logger.debug(s"on job start, dag-task-id: $taskId, jobId: ${jobStart.jobId}")
      map += (taskId -> jobStart.jobId)
    }
  }

  override def onJobEnd(jobEnd: SparkListenerJobEnd): Unit = {
    logger.debug(s"on job end, jobId: ${jobEnd.jobId}")
    map.find(_._2 == jobEnd.jobId).foreach { e =>
      map -= e._1
    }
  }

  def getJobId(taskId: String): Option[Int] = map.get(taskId)

}
