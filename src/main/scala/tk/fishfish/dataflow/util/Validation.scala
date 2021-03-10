package tk.fishfish.dataflow.util

/**
 * 参数校验
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object Validation {

  def notNull(value: Any): Unit = notNull(value, "参数不能为空")

  def notNull(value: Any, message: String): Unit = {
    if (value == null) {
      throw new IllegalArgumentException(message)
    }
  }

  def notEmpty(value: String): Unit = notEmpty(value, "参数不能为空")

  def notEmpty(value: String, message: String): Unit = {
    if (value == null || value.isEmpty) {
      throw new IllegalArgumentException(message)
    }
  }

  def notEmpty(value: Seq[_]): Unit = notEmpty(value, "参数不能为空")

  def notEmpty(value: Seq[_], message: String): Unit = {
    if (value == null || value.isEmpty) {
      throw new IllegalArgumentException(message)
    }
  }

}
