package tk.fishfish.dataflow.dag

import com.google.common.graph.MutableGraph
import org.apache.commons.lang3.StringUtils
import tk.fishfish.dataflow.core.Conf

import java.util.Objects
import scala.collection.mutable

/**
 * 任务流
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
case class Graph(nodes: Seq[Node], edges: Seq[Edge])

case class Node(id: String, name: String, nodeType: String, conf: Conf) {

  override def hashCode(): Int = Objects.hash(id)

  override def equals(obj: Any): Boolean = {
    if (obj == null || !obj.isInstanceOf[Node]) {
      return false
    }
    StringUtils.equals(this.id, obj.asInstanceOf[Node].id)
  }

}

case class Edge(from: String, to: String, conf: Conf) {

  override def hashCode(): Int = Objects.hash(from, to)

  override def equals(obj: Any): Boolean = {
    if (obj == null || !obj.isInstanceOf[Edge]) {
      return false
    }
    val edge = obj.asInstanceOf[Edge]
    StringUtils.equals(this.from, edge.from) && StringUtils.equals(this.to, edge.to)
  }

}

// noinspection UnstableApiUsage
// https://blog.csdn.net/qq_20087731/article/details/91489573
class GraphPath(val graph: MutableGraph[String], val from: String, val to: String) {

  /**
   * 已访问过的节点
   */
  private val visit = mutable.Set[String]()

  /**
   * 每条可能的路径
   */
  private val path = new Array[String](graph.nodes().size())

  private var result: Seq[String] = Seq()

  private var top = 0

  def visitPath(): Seq[String] = {
    dfs(from)
    result
  }

  def dfs(pos: String): Unit = {
    visit += pos
    path(top) = pos
    top += 1
    if (pos.equals(to)) {
      // 到达终点
      result = path.take(top).toSeq
      return
    }
    import scala.collection.JavaConversions.asScalaSet
    for (node <- graph.nodes()) {
      // 没有访问过，且相邻
      if (!visit.contains(node) && graph.hasEdgeConnecting(pos, node)) {
        dfs(node)
      }
    }
    visit -= pos
    top -= 1
  }

}
