package tk.fishfish.dataflow.database.sql

import tk.fishfish.dataflow.util.Properties

/**
 * SQL执行器
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Executor extends Serializable {

  def execute(sql: String*): Unit

}

trait ExecutorFactory extends Serializable {

  def accept(url: String): Boolean

  def create(props: Properties): Executor

}
