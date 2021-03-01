package tk.fishfish.dataflow.dag

import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.collect.{HashMultimap, SetMultimap, Sets}
import com.google.common.graph.{GraphBuilder, Graphs, MutableGraph}
import org.apache.commons.lang3.StringUtils
import tk.fishfish.dataflow.exception.DagException

import scala.beans.BeanProperty
import scala.collection.mutable

/**
 * DAG
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
   * 节点后继
   */
  private val successors: SetMultimap[Node, Node] = HashMultimap.create[Node, Node]

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
    successors.removeAll(node)
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
    // 前驱、后继、边
    for (edge <- graph.edges) {
      val from = nodeMap.get(edge.from).orNull
      val to = nodeMap.get(edge.to).orNull
      if (from == null || to == null) {
        throw new DagException(s"边无效: ${edge.from}~${edge.to}")
      }
      dag.predecessors.put(to, from)
      dag.successors.put(from, to)
      dag.edges.add(edge)
    }
    dag
  }

  // noinspection UnstableApiUsage
  def simplePaths(graph: Graph): Seq[Seq[String]] = {
    val g: MutableGraph[String] = GraphBuilder.directed()
      .allowsSelfLoops(false)
      .expectedNodeCount(10)
      .build()
    val nodeMap = mutable.Map[String, Node]()
    for (node <- graph.nodes) {
      nodeMap += (node.id -> node)
      g.addNode(node.id)
    }
    for (edge <- graph.edges) {
      val from = nodeMap.get(edge.from).orNull
      val to = nodeMap.get(edge.to).orNull
      if (from == null || to == null) {
        throw new DagException(s"边无效: ${edge.from}~${edge.to}")
      }
      g.putEdge(edge.from, edge.to)
    }
    if (Graphs.hasCycle(g)) {
      throw new DagException("不能存在环, 必须是一个有向五环图")
    }
    // 找出起始顶点，无前驱只有后继; 找出目标顶点，只有前驱而无后继
    var starts = mutable.Seq[String]()
    var ends = mutable.Seq[String]()
    for (node <- graph.nodes) {
      // 前驱
      val predecessors = g.predecessors(node.id)
      val successors = g.successors(node.id)
      if (predecessors != null && predecessors.size() >= 2) {
        throw new DagException(s"不支持多入度，检测到节点${node.id}入度为: ${predecessors.size()}")
      }
      if (g.degree(node.id) != 0) {
        // 后继
        if (isEmpty(predecessors) && !isEmpty(successors)) {
          starts = starts :+ node.id
        }
        if (!isEmpty(predecessors) && isEmpty(successors)) {
          ends = ends :+ node.id
        }
      }
    }
    // 找出2点之间的简单路径
    var paths = mutable.Seq[Seq[String]]()
    for (from <- starts) {
      for (to <- ends) {
        val path = new GraphPath(g, from, to).visitPath()
        if (path.nonEmpty) {
          paths = paths :+ path
        }
      }
    }
    paths
  }

  def isEmpty(collection: java.util.Set[_]): Boolean = collection == null || collection.isEmpty

}
