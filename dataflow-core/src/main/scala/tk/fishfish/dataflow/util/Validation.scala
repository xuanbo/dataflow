package tk.fishfish.dataflow.util

/**
 * 参数校验
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object Validation {

  def nonNull(value: Any): Unit = nonNull(value, "参数不能为空")

  def nonNull(value: Any, message: String): Unit = {
    if (value == null) {
      throw new IllegalArgumentException(message)
    }
  }

  def nonEmpty(value: String): Unit = nonEmpty(value, "参数不能为空")

  def nonEmpty(value: String, message: String): Unit = {
    if (value == null || value.isEmpty) {
      throw new IllegalArgumentException(message)
    }
  }

  def nonEmpty(value: Seq[_]): Unit = nonEmpty(value, "参数不能为空")

  def nonEmpty(value: Seq[_], message: String): Unit = {
    if (value == null || value.isEmpty) {
      throw new IllegalArgumentException(message)
    }
  }

}
