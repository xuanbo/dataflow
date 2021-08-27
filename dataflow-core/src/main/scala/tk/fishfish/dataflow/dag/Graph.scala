package tk.fishfish.dataflow.dag

import org.apache.commons.lang3.StringUtils
import tk.fishfish.dataflow.task.Argument

import java.util.Objects

/**
 * 任务流
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
case class Graph(nodes: Seq[Node], edges: Seq[Edge])

case class Node(id: String, name: String, text: String, argument: Argument) {

  override def hashCode(): Int = Objects.hash(id)

  override def equals(obj: Any): Boolean = {
    if (obj == null || !obj.isInstanceOf[Node]) {
      return false
    }
    StringUtils.equals(this.id, obj.asInstanceOf[Node].id)
  }

}

case class Edge(from: String, to: String) {

  override def hashCode(): Int = Objects.hash(from, to)

  override def equals(obj: Any): Boolean = {
    if (obj == null || !obj.isInstanceOf[Edge]) {
      return false
    }
    val edge = obj.asInstanceOf[Edge]
    StringUtils.equals(this.from, edge.from) && StringUtils.equals(this.to, edge.to)
  }

}
