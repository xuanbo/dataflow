package tk.fishfish.dataflow.dag

import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.collect.{HashMultimap, SetMultimap, Sets}
import org.apache.commons.lang3.StringUtils
import tk.fishfish.dataflow.exception.DagException

import scala.beans.BeanProperty
import scala.collection.mutable

/**
 * 描述
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class Dag {

  /**
   * 节点
   */
  @BeanProperty
  val nodes: java.util.Set[Node] = Sets.newHashSet()

  /**
   * 边
   */
  @BeanProperty
  val edges: java.util.Set[Edge] = Sets.newHashSet()

  /**
   * 节点前驱
   */
  private val predecessors: SetMultimap[Node, Node] = HashMultimap.create[Node, Node]

  /**
   * 节点指向的边
   */
  private val connections: SetMultimap[Node, (Node, Edge)] = HashMultimap.create[Node, (Node, Edge)]

  /**
   * 返回下一个要执行的节点
   *
   * @return 节点
   */
  def poll(): Seq[Node] = {
    var res = mutable.Seq[Node]()
    import scala.collection.convert.ImplicitConversions.`set asScala`
    for (node <- nodes) {
      val pres = predecessors.get(node)
      if (pres == null || pres.isEmpty) {
        res = res :+ node
      }
    }
    res
  }

  @JsonIgnore
  def isComplete: Boolean = nodes.isEmpty

  def complete(node: Node): Unit = {
    nodes.remove(node)
    predecessors.entries.removeIf(e => e.getValue.equals(node))
    // 移除节点指向的边
    import scala.collection.convert.ImplicitConversions.`set asScala`
    connections.get(node).map(e => e._2).foreach(e => edges.remove(e))
    connections.entries.removeIf(e => e.getKey.equals(node))
  }

}

object Dag {

  def apply(graph: Graph): Dag = {
    val dag = new Dag()
    if (graph.nodes == null || graph.nodes.isEmpty) {
      throw new DagException("节点为空")
    }
    if (graph.edges == null || graph.edges.isEmpty) {
      throw new DagException("边为空")
    }
    val nodeMap = mutable.Map[String, Node]()
    // 节点
    for (node <- graph.nodes) {
      if (StringUtils.isAnyBlank(node.id, node.name, node.nodeType)) {
        throw new DagException(s"节点无效, 参数不合法")
      }
      dag.nodes.add(node)
      nodeMap += (node.id -> node)
    }
    // 前驱、节点指向的边
    for (edge <- graph.edges) {
      val from = nodeMap.get(edge.from).orNull
      val to = nodeMap.get(edge.to).orNull
      if (from == null || to == null) {
        throw new DagException(s"边无效: ${edge.from}~${edge.to}")
      }
      dag.predecessors.put(to, from)
      dag.connections.put(from, (to, edge))
      dag.edges.add(edge)
    }
    dag
  }

}
