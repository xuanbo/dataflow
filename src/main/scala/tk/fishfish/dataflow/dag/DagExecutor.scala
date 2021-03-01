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

  def run(dag: Dag): Unit

}

class DefaultDagExecutor(tasks: Seq[Task]) extends DagExecutor {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DefaultDagExecutor])

  private val taskMap: Map[String, Task] = tasks.map(x => (x.taskType(), x)).toMap

  def run(dag: Dag): Unit = {
    logger.debug("run starting")
    var df: DataFrame = null
    try {
      while (!dag.isComplete) {
        val nodes = dag.poll()
        for (node <- nodes) {
          taskMap.get(node.nodeType) match {
            case Some(task) => task match {
              case source: Source =>
                df = source.read(node.conf)
                df.persist(StorageLevel.MEMORY_AND_DISK)
                val total = df.count()
                logger.info("源端读取数据条数: {}", total)
              case transformer: Transformer =>
                df = transformer.transform(df, node.conf)
              case filter: Filter =>
                df = filter.filter(df, node.conf)
              case target: Target =>
                target.write(df, node.conf)
            }
            case None => throw new DagException(s"节点不支持的类型: ${node.nodeType}")
          }
          dag.complete(node)
        }
      }
    } catch {
      case e: Exception => {
        logger.warn("run error", e)
      }
    } finally {
      logger.debug("run finishing")
      if (df != null) {
        df.unpersist()
      }
    }
  }

}
