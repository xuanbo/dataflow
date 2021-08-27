package tk.fishfish.dataflow.dag

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.scheduling.annotation.Async
import tk.fishfish.dataflow
import tk.fishfish.dataflow.entity.Execution
import tk.fishfish.dataflow.entity.enums.ExecuteStatus
import tk.fishfish.dataflow.exception.DagException
import tk.fishfish.dataflow.service.{ExecutionService, TaskService}
import tk.fishfish.dataflow.task.{Result, Task}
import tk.fishfish.dataflow.util.HttpUtils
import tk.fishfish.json.util.JSON

import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable

/**
 * DAG执行器
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait DagExecutor {

  @Async
  def run(param: ExecutionParam): Unit

  def cancel(executionId: String): Unit

}

abstract class AbstractDagExecutor(val spark: SparkSession,
                                   val tasks: Map[String, Task],
                                   val executionService: ExecutionService,
                                   val taskService: TaskService)
  extends DagExecutor {

  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  protected val autoIncrement: AtomicLong = new AtomicLong();

  protected def initContext(param: ExecutionParam, namespace: String): Unit = {
    param.context += ("graphId" -> param.graphId)
    param.context += ("executionId" -> param.executionId)
    param.context += ("namespace" -> namespace)
  }

  protected def initNode(param: ExecutionParam, namespace: String, node: Node, taskId: String): Unit = {
    node.argument.namespace = namespace
    val ctxStr = JSON.write(param.context)
    node.argument.context = JSON.read(ctxStr, classOf[mutable.Map[String, Any]])
    node.argument.context += ("nodeId" -> node.id)
    node.argument.context += ("taskId" -> taskId)
  }

  protected def runTask(node: Node): Result = {
    tasks.get(node.name) match {
      case Some(task) => task.execute(node.argument)
      case None => throw new DagException(s"节点不支持的类型: ${node.name}")
    }
  }

  protected def startExecution(param: ExecutionParam): Unit = {
    val execution = new Execution()
    execution.setId(param.executionId)
    execution.setGraphId(param.graphId)
    execution.setGraphContent(JSON.write(param.graph))
    execution.setStatus(ExecuteStatus.RUNNING)
    executionService.insertSelective(execution)
  }

  protected def startTask(executionId: String, node: Node): String = {
    val task = new dataflow.entity.Task()
    task.setExecutionId(executionId)
    task.setNodeId(node.id)
    task.setNodeName(node.name)
    task.setNodeText(node.text)
    task.setStatus(ExecuteStatus.RUNNING)
    taskService.insertSelective(task)
    task.getId
  }

  protected def endTask(taskId: String, status: ExecuteStatus, message: String, result: Result): Unit = {
    val task = new dataflow.entity.Task()
    task.setId(taskId)
    task.setStatus(status)
    task.setMessage(message)
    Option(result).map(_.data).foreach(task.setData)
    Option(result).map(_.numbers).foreach(task.setNumbers(_))
    taskService.updateSelective(task)
  }

  protected def endExecution(executionId: String, message: String): Execution = {
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
    execution
  }

  protected def doCallback(callback: String, execution: Execution): Unit = {
    if (StringUtils.isBlank(callback)) {
      return
    }
    try {
      HttpUtils.post(callback, execution, classOf[String])
    } catch {
      case e: Exception => logger.warn("触发回调失败: {}", e.getMessage)
    }
  }

}
