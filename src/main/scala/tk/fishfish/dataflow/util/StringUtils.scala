package tk.fishfish.dataflow.util

/**
 * 字符串工具类
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object StringUtils {

  def isEmpty(value: String): Boolean = value == null || value.isEmpty

  def isNotEmpty(value: String): Boolean = !isEmpty(value)

}
