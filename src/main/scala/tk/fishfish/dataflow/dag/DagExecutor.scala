package tk.fishfish.dataflow.dag

import org.apache.spark.sql.DataFrame
import org.apache.spark.storage.StorageLevel
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.core.{Filter, Source, Target, Task, Transformer}
import tk.fishfish.dataflow.entity.enums.ExecuteStatus
import tk.fishfish.dataflow.entity.{Execution, Flow}
import tk.fishfish.dataflow.exception.DagException
import tk.fishfish.dataflow.service.{ExecutionService, FlowService}

import java.util.Date
import scala.collection.mutable

/**
 * DAG执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait DagExecutor {

  def run(graphId: String, graph: Graph): Unit

}

class DefaultDagExecutor(tasks: Seq[Task], executionService: ExecutionService, flowService: FlowService)
  extends DagExecutor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DefaultDagExecutor])

  private val taskMap: Map[String, Task] = tasks.map(x => (x.taskType(), x)).toMap

  def run(graphId: String, graph: Graph): Unit = {
    val paths = Dag.simplePaths(graph)
    val execution = startExecution(graphId)
    var executionStatus = ExecuteStatus.SUCCESS
    val map = graph.nodes.map { e => (e.id, e) }.toMap
    val needCacheNodes = analyseCacheNodes(paths)
    logger.info("需要缓存的节点: {}", needCacheNodes.mkString(", "))
    val nodeDF = mutable.Map[String, DataFrame]()
    val cacheNodes = mutable.Set[DataFrame]()
    for (path <- paths) {
      val flowPath = path.mkString(" -> ")
      logger.info("运行任务流: {}", flowPath)
      val flow = startFlow(execution.getId, flowPath)
      var df: DataFrame = null
      try {
        for (id <- path) {
          nodeDF.get(id) match {
            case Some(tmpDF) => df = tmpDF
            case None => {
              map.get(id) match {
                case Some(node) => {
                  taskMap.get(node.nodeType) match {
                    case Some(task) => task match {
                      case source: Source => df = source.read(node.conf)
                      case transformer: Transformer => df = transformer.transform(df, node.conf)
                      case filter: Filter => df = filter.filter(df, node.conf)
                      case target: Target => target.write(df, node.conf)
                    }
                    case None => throw new DagException(s"节点不支持的类型: ${node.nodeType}")
                  }
                }
              }
              nodeDF += (id -> df)
            }
          }
          if (needCacheNodes.contains(id)) {
            // 保证只缓存一次
            if (!cacheNodes.contains(df)) {
              df.persist(StorageLevel.MEMORY_AND_DISK)
              val total = df.count()
              logger.info("缓存节点: {}, 数据条数: {}", id, total)
            }
            cacheNodes += df
          }
        }
        flow.setStatus(ExecuteStatus.SUCCESS)
      } catch {
        case e: Exception => {
          logger.warn("运行任务流失败", e)
          flow.setStatus(ExecuteStatus.ERROR)
          flow.setMessage(e.getMessage)
          executionStatus = ExecuteStatus.ERROR
        }
      } finally {
        // 清理缓存
        cacheNodes.foreach(_.unpersist())
        endFlow(flow)
      }
    }
    execution.setStatus(executionStatus)
    endExecution(execution)
  }

  private[this] def analyseCacheNodes(paths: Seq[Seq[String]]): Set[String] = {
    val nodes = mutable.Set[String]()
    val cacheNodes = mutable.Set[String]()
    for (path <- paths) {
      for (node <- path) {
        if (nodes.contains(node)) {
          cacheNodes += node
        }
        nodes += node
      }
    }
    cacheNodes.toSet
  }

  private[this] def startExecution(graphId: String): Execution = {
    val execution = new Execution()
    execution.setGraphId(graphId)
    execution.setStatus(ExecuteStatus.RUNNING)
    execution.setCreateTime(new Date())
    executionService.insert(execution)
    execution
  }

  private[this] def endExecution(execution: Execution): Unit = {
    execution.setUpdateTime(new Date())
    executionService.update(execution)
  }

  private[this] def startFlow(executionId: String, flowPath: String): Flow = {
    val flow = new Flow()
    flow.setExecutionId(executionId)
    flow.setPath(flowPath)
    flow.setStatus(ExecuteStatus.RUNNING)
    flow.setCreateTime(new Date())
    flowService.insert(flow)
    flow
  }

  private[this] def endFlow(flow: Flow): Unit = {
    flow.setUpdateTime(new Date())
    flowService.update(flow)
  }

}
