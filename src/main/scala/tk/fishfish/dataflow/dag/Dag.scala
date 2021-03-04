package tk.fishfish.dataflow.dag

import com.google.common.graph.{GraphBuilder, Graphs, MutableGraph}
import tk.fishfish.dataflow.exception.DagException

import scala.collection.mutable

object Dag {

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
