package tk.fishfish.dataflow.dag

import org.apache.spark.sql.DataFrame
import org.apache.spark.storage.StorageLevel
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.core.{Filter, Source, Target, Task, Transformer}
import tk.fishfish.dataflow.exception.DagException

import scala.collection.mutable

/**
 * DAG执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait DagExecutor {

  def run(graph: Graph): Unit

}

class DefaultDagExecutor(tasks: Seq[Task]) extends DagExecutor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DefaultDagExecutor])

  private val taskMap: Map[String, Task] = tasks.map(x => (x.taskType(), x)).toMap

  def run(graph: Graph): Unit = {
    val paths = Dag.simplePaths(graph)
    val map = graph.nodes.map { e => (e.id, e) }.toMap
    val needCacheNodes = analyseCacheNodes(paths)
    logger.info("需要缓存的节点: {}", needCacheNodes.mkString(", "))
    val nodeDF = mutable.Map[String, DataFrame]()
    val cacheNodes = mutable.Set[DataFrame]()
    for (flow <- paths) {
      logger.info("运行任务流: {}", flow.mkString(" -> "))
      var df: DataFrame = null
      try {
        for (id <- flow) {
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
      } catch {
        case e: Exception => {
          logger.warn("运行任务流失败", e)
        }
      } finally {
        // 清理缓存
        cacheNodes.foreach(_.unpersist())
      }
    }
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

}
