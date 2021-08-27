package tk.fishfish.dataflow.dag

import org.apache.spark.sql.SparkSession
import tk.fishfish.dataflow.concurrent.Cancellable
import tk.fishfish.dataflow.entity.enums.ExecuteStatus
import tk.fishfish.dataflow.service.{ExecutionService, TaskService}
import tk.fishfish.dataflow.task.{Result, Task}
import tk.fishfish.dataflow.util.{LockUtils, SparkUtils}

import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.{CancellationException, ConcurrentHashMap}
import scala.collection.mutable.ListBuffer
import scala.collection.{concurrent, mutable}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.Breaks.{break, breakable}
import scala.util.{Failure, Success}

/**
 * 多线程实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class ConcurrentDagExecutor(override val spark: SparkSession,
                            override val tasks: Map[String, Task],
                            override val executionService: ExecutionService,
                            override val taskService: TaskService,
                            val sparkListener: DagSparkListener)
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
    var message: String = null

    // state control
    val readWriteLock = new ReentrantReadWriteLock()
    val submittedNodes = mutable.Set[String]()
    val nodeErrors = mutable.Map[String, String]()
    val nodeFutures = new ListBuffer[Cancellable[Result]]()

    initContext(param, namespace)
    try {
      SparkUtils.createDatabase(spark, namespace)
      val dag = Dag(param.graph)
      breakable {
        while (!dag.isComplete) {
          LockUtils.using(readWriteLock.readLock()) {
            if (nodeErrors.nonEmpty) {
              break()
            }
          }
          val nodes = dag.poll()
          for (node <- nodes) {
            val submitted = LockUtils.using(readWriteLock.readLock()) {
              submittedNodes.contains(node.id)
            }
            if (!submitted) {
              LockUtils.using(readWriteLock.writeLock()) {
                submittedNodes += node.id
              }
              val taskId = startTask(param.executionId, node)
              initNode(param, namespace, node, taskId)
              val cancellable = Cancellable {
                runTask(node)
              }
              LockUtils.using(readWriteLock.writeLock()) {
                nodeFutures += cancellable
              }
              cancellable.future.onComplete {
                case Success(result) => {
                  endTask(taskId, ExecuteStatus.SUCCESS, null, result)
                  dag.complete(node)
                }
                case Failure(exception) => {
                  val message = exception match {
                    case _: CancellationException => {
                      sparkListener.getJobId(taskId).foreach { jobId =>
                        logger.info(s"spark job $jobId cancelling")
                        spark.sparkContext.cancelJob(jobId)
                      }
                      "dag task cancelled"
                    }
                    case e: Throwable => {
                      logger.warn(s"执行任务错误", exception)
                      LockUtils.using(readWriteLock.readLock()) {
                        for (cancellable <- nodeFutures) {
                          cancellable.cancel()
                        }
                      }
                      LockUtils.using(readWriteLock.writeLock()) {
                        nodeErrors += (node.id -> e.getMessage)
                      }
                      e.getMessage
                    }
                  }
                  endTask(taskId, ExecuteStatus.ERROR, message, null)
                  dag.complete(node)
                }
              }
            }
          }
          logger.debug("等待子任务并行执行完毕，任务流: {}", param.executionId)
          Thread.sleep(500)
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
        LockUtils.using(readWriteLock.readLock()) {
          for (cancellable <- nodeFutures) {
            cancellable.cancel()
          }
        }
      }
    } finally {
      SparkUtils.dropDatabase(spark, namespace)
      logger.info("任务流结束: {}", param.executionId)
      message = LockUtils.using(readWriteLock.readLock()) {
        if (nodeErrors.nonEmpty) {
          nodeErrors.values.head
        } else {
          message
        }
      }
      val execution = endExecution(param.executionId, message)
      doCallback(param.callback, execution)
    }
  }

  override def cancel(executionId: String): Unit = {
    logger.info("cancelling execution: {}", executionId)
    dispatchThreads.get(executionId) match {
      case Some(thread) => {
        thread.interrupt()
        logger.info("interrupt dispatch thread: {}", thread.getName)
      }
      case None => logger.info("dispatch thread not found")
    }
  }

}
