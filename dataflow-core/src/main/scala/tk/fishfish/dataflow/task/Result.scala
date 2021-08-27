package tk.fishfish.dataflow.task

/**
 * 结果
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
case class Result(data: String, numbers: Long)

object Result {

  def empty(): Result = Result(null, 0)

}
