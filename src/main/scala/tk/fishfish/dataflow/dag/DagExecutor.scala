package tk.fishfish.dataflow.dag

import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow
import tk.fishfish.dataflow.core.{Filter, Source, Target, Task, Transformer}
import tk.fishfish.dataflow.entity.enums.ExecuteStatus
import tk.fishfish.dataflow.exception.DagException
import tk.fishfish.dataflow.service.{ExecutionService, TaskService}

import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable

/**
 * DAG执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait DagExecutor {

  def run(executionId: String, dag: Dag): String

}

class DefaultDagExecutor(val spark: SparkSession, val tasks: Map[String, Task],
                         val executionService: ExecutionService, val taskService: TaskService)
  extends DagExecutor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DefaultDagExecutor])

  private[this] val autoIncrement: AtomicLong = new AtomicLong();

  def run(executionId: String, dag: Dag): String = {
    val namespace = s"n_${autoIncrement.incrementAndGet()}"
    logger.info("运行任务流: {}", executionId)
    val tables = mutable.Set[String]()
    var status: ExecuteStatus = null
    var message: String = null
    try {
      while (!dag.isComplete) {
        val nodes = dag.poll()
        for (node <- nodes) {
          val taskId = startTask(executionId, node).getId
          try {
            tables ++= runTask(namespace, node)
            status = ExecuteStatus.RUNNING
          } catch {
            case e: Exception => {
              status = ExecuteStatus.ERROR
              message = e.getMessage
              throw e
            }
          } finally {
            endTask(taskId, status, message)
            dag.complete(node)
          }
        }
      }
    } catch {
      case e: Exception => {
        logger.warn(s"任务流运行失败: $executionId", e)
      }
    } finally {
      // 清理临时表
      for (table <- tables) {
        spark.sql(s"DROP TABLE IF EXISTS $table")
      }
      logger.info("任务流结束: {}", executionId)
    }
    message
  }

  private[this] def runTask(namespace: String, node: Node): Seq[String] = {
    tasks.get(node.name) match {
      case Some(task) => task match {
        case source: Source =>
          node.argument.namespace = namespace
          source.read(node.argument)
          node.argument.tables
        case transformer: Transformer =>
          node.argument.namespace = namespace
          transformer.transform(node.argument)
          node.argument.tables
        case filter: Filter =>
          node.argument.namespace = namespace
          filter.filter(node.argument)
          node.argument.tables
        case target: Target =>
          node.argument.namespace = namespace
          target.write(node.argument)
          node.argument.tables
        case _ => throw new DagException(s"节点不支持的类型: ${node.name}")
      }
      case None => throw new DagException(s"节点不支持的类型: ${node.name}")
    }
  }

  private[this] def startTask(executionId: String, node: Node): dataflow.entity.Task = {
    val task = new dataflow.entity.Task()
    task.setExecutionId(executionId)
    task.setNodeId(node.id)
    task.setNodeName(node.name)
    task.setNodeText(node.text)
    task.setStatus(ExecuteStatus.RUNNING)
    taskService.insert(task)
    task
  }

  private[this] def endTask(taskId: String, status: ExecuteStatus, message: String): Unit = {
    val task = new dataflow.entity.Task()
    task.setId(taskId)
    task.setStatus(status)
    task.setMessage(message)
    taskService.updateSelective(task)
  }

}
