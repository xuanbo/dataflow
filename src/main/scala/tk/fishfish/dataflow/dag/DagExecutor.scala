package tk.fishfish.dataflow.dag

import org.apache.spark.sql.DataFrame
import org.apache.spark.storage.StorageLevel
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.core.{Filter, Source, Target, Task, Transformer}
import tk.fishfish.dataflow.exception.DagException

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
    for (flow <- paths) {
      logger.info("flow: {}", flow.mkString(" -> "))
      var df, sourceDF: DataFrame = null
      try {
        for (id <- flow) {
          map.get(id) match {
            case Some(node) => {
              taskMap.get(node.nodeType) match {
                case Some(task) => task match {
                  case source: Source =>
                    sourceDF = source.read(node.conf)
                    sourceDF.persist(StorageLevel.MEMORY_AND_DISK)
                    val total = sourceDF.count()
                    logger.info("源端读取数据条数: {}", total)
                    df = sourceDF
                  case transformer: Transformer =>
                    df = transformer.transform(df, node.conf)
                  case filter: Filter =>
                    df = filter.filter(df, node.conf)
                  case target: Target =>
                    target.write(df, node.conf)
                }
                case None => throw new DagException(s"节点不支持的类型: ${node.nodeType}")
              }
            }
          }
        }
      } catch {
        case e: Exception => {
          logger.warn("运行任务流失败", e)
        }
      } finally {
        if (sourceDF != null) {
          sourceDF.unpersist()
        }
      }
    }
  }

}
