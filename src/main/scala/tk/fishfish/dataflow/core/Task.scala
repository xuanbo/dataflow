package tk.fishfish.dataflow.core

/**
 * 任务
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Task extends Serializable {

  /**
   * 任务名称
   *
   * @return 名称
   */
  def name(): String

}

