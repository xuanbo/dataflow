package tk.fishfish.dataflow.dag

import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.scheduling.annotation.Async
import tk.fishfish.dataflow.core.{Filter, Source, Target, Transformer}
import tk.fishfish.dataflow.entity.Execution
import tk.fishfish.dataflow.entity.enums.ExecuteStatus
import tk.fishfish.dataflow.exception.DagException
import tk.fishfish.dataflow.service.{ExecutionService, TaskService}
import tk.fishfish.dataflow.util.SparkUtils
import tk.fishfish.dataflow.{core, entity}
import tk.fishfish.json.util.JSON

import java.util.concurrent.atomic.AtomicLong
import javax.validation.constraints.{NotBlank, NotNull}
import scala.collection.mutable

/**
 * DAG执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait DagExecutor {

  @Async
  def run(param: ExecutionParam): Unit

}

case class ExecutionParam(@NotBlank executionId: String, graphId: String, context: mutable.Map[String, Any], @NotNull graph: Graph)

class DefaultDagExecutor(val spark: SparkSession, val tasks: Map[String, core.Task],
                         val executionService: ExecutionService, val taskService: TaskService)
  extends DagExecutor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DefaultDagExecutor])

  private[this] val autoIncrement: AtomicLong = new AtomicLong()

  def run(param: ExecutionParam): Unit = {
    logger.info("运行任务流: {}", param.executionId)
    startExecution(param)
    param.context += ("graphId" -> param.graphId)
    param.context += ("executionId" -> param.executionId)
    val namespace = s"n_${autoIncrement.incrementAndGet()}"
    val tables = mutable.Set[String]()
    var status: ExecuteStatus = null
    var message: String = null
    try {
      val dag = Dag(param.graph)
      while (!dag.isComplete) {
        val nodes = dag.poll()
        for (node <- nodes) {
          val taskId = startTask(param.executionId, node)
          try {
            node.argument.namespace = namespace
            node.argument.context = param.context
            runTask(node)
            status = ExecuteStatus.SUCCESS
          } catch {
            case e: Exception =>
              status = ExecuteStatus.ERROR
              message = e.getMessage
              throw e
          } finally {
            endTask(taskId, status, message)
            dag.complete(node)
          }
        }
      }
    } catch {
      case e: Exception => {
        logger.warn(s"任务流运行失败: ${param.executionId}", e)
        message = e.getMessage
      }
    } finally {
      // 清理临时表
      SparkUtils.cleanup(spark, namespace)
      logger.info("任务流结束: {}", param.executionId)
      endExecution(param.executionId, message)
    }
  }

  private[this] def runTask(node: Node): Unit = {
    tasks.get(node.name) match {
      case Some(task) => task match {
        case source: Source => source.read(node.argument)
        case transformer: Transformer => transformer.transform(node.argument)
        case filter: Filter => filter.filter(node.argument)
        case target: Target => target.write(node.argument)
        case _ => throw new DagException(s"节点不支持的类型: ${node.name}")
      }
      case None => throw new DagException(s"节点不支持的类型: ${node.name}")
    }
  }

  private[this] def startExecution(param: ExecutionParam): Unit = {
    val execution = new Execution()
    execution.setId(param.executionId)
    execution.setGraphId(param.graphId)
    execution.setGraphContent(JSON.write(param.graph))
    execution.setStatus(ExecuteStatus.RUNNING)
    executionService.insertSelective(execution)
  }

  private[this] def startTask(executionId: String, node: Node): String = {
    val task = new entity.Task()
    task.setExecutionId(executionId)
    task.setNodeId(node.id)
    task.setNodeName(node.name)
    task.setNodeText(node.text)
    task.setStatus(ExecuteStatus.RUNNING)
    taskService.insertSelective(task)
    task.getId
  }

  private[this] def endTask(taskId: String, status: ExecuteStatus, message: String): Unit = {
    val task = new entity.Task()
    task.setId(taskId)
    task.setStatus(status)
    task.setMessage(message)
    taskService.updateSelective(task)
  }

  private[this] def endExecution(executionId: String, message: String): Unit = {
    val execution = new Execution()
    execution.setId(executionId)
    val status = if (message == null) {
      ExecuteStatus.SUCCESS
    } else {
      ExecuteStatus.ERROR
    }
    execution.setStatus(status)
    execution.setMessage(message)
    executionService.updateSelective(execution)
  }

}
