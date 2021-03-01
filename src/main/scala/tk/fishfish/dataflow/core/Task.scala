package tk.fishfish.dataflow.core

/**
 * 任务
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Task extends Serializable {

  /**
   * 任务类型
   *
   * @return 名称
   */
  def taskType(): String

  /**
   * 支持后继节点类型
   *
   * @return
   */
  def supportNext(): Seq[Class[_]]

}

