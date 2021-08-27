package tk.fishfish.dataflow.dag

import org.apache.spark.sql.SparkSession
import tk.fishfish.dataflow.entity.enums.ExecuteStatus
import tk.fishfish.dataflow.service.{ExecutionService, TaskService}
import tk.fishfish.dataflow.task.{Result, Task}
import tk.fishfish.dataflow.util.SparkUtils

import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent

/**
 * 单线程实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class SimpleDagExecutor(override val spark: SparkSession,
                        override val tasks: Map[String, Task],
                        override val executionService: ExecutionService,
                        override val taskService: TaskService)
  extends AbstractDagExecutor(spark, tasks, executionService, taskService) {

  private val dispatchThreads: concurrent.Map[String, Thread] = {
    import scala.collection.JavaConversions.mapAsScalaConcurrentMap
    new ConcurrentHashMap[String, Thread]()
  }

  def run(param: ExecutionParam): Unit = {
    logger.info("运行任务流: {}", param.executionId)
    dispatchThreads += (param.executionId -> Thread.currentThread())
    startExecution(param)
    val namespace = s"db${autoIncrement.incrementAndGet()}"
    var status: ExecuteStatus = null
    var result: Result = null
    var message: String = null
    initContext(param, namespace)
    try {
      SparkUtils.createDatabase(spark, namespace)
      val dag = Dag(param.graph)
      while (!dag.isComplete) {
        val nodes = dag.poll()
        for (node <- nodes) {
          val taskId = startTask(param.executionId, node)
          try {
            initNode(param, namespace, node, taskId)
            result = runTask(node)
            status = ExecuteStatus.SUCCESS
          } catch {
            case e: Exception =>
              status = ExecuteStatus.ERROR
              message = e.getMessage
              throw e
          } finally {
            endTask(taskId, status, message, result)
            dag.complete(node)
          }
        }
      }
    } catch {
      case e: Throwable => {
        logger.warn(s"任务流运行失败: ${param.executionId}", e)
        message = if (e.isInstanceOf[InterruptedException]) {
          "execution cancelled"
        } else {
          e.getMessage
        }
      }
    } finally {
      SparkUtils.dropDatabase(spark, namespace)
      logger.info("任务流结束: {}", param.executionId)
      val execution = endExecution(param.executionId, message)
      doCallback(param.callback, execution)
      dispatchThreads -= param.executionId
    }
  }

  override def cancel(executionId: String): Unit = {
    logger.info("cancelling execution: {}", executionId)
    dispatchThreads.get(executionId) match {
      case Some(thread) => {
        thread.interrupt()
        logger.info("interrupt dispatch thread: {}", thread.getName)
      }
      case None => logger.info("cannot find dispatch thread")
    }
  }

}
