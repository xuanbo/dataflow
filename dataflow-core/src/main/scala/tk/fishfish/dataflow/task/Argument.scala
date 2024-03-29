package tk.fishfish.dataflow.task

import com.fasterxml.jackson.annotation.JsonInclude
import tk.fishfish.json.util.JSON

import scala.beans.BeanProperty
import scala.collection.mutable

/**
 * 配置
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class Argument {

  /**
   * 命名空间，会用作表前缀，进行数据隔离
   */
  @BeanProperty
  var namespace: String = _

  /**
   * 上下文
   */
  var context: mutable.Map[String, Any] = _

  /**
   * 输入参数
   */
  @BeanProperty
  var input: Configuration = _

  /**
   * 输出参数
   */
  @BeanProperty
  var output: Configuration = _

}

class Configuration extends java.util.HashMap[String, Any] {

  def getString(key: String): String = getString(key, null)

  def getString(key: String, default: String): String = {
    val value = get(key)
    if (value == null) {
      return default
    }
    value.toString
  }

  def getInt(key: String, default: Int): Int = {
    val value = get(key)
    if (value == null) {
      return default
    }
    Integer.parseInt(value.toString)
  }

  def get[T](key: String, clazz: Class[T]): Option[T] = {
    val value = get(key)
    if (value == null) {
      return None
    }
    val json = JSON.write(value)
    Some(JSON.read(json, clazz))
  }

  def getSeq(key: String): Seq[Configuration] = getSeq(key, classOf[Configuration])

  def getSeq[T](key: String, clazz: Class[T]): Seq[T] = {
    val value = get(key)
    if (value == null) {
      return Seq.empty
    }
    val json = JSON.write(value)
    import scala.collection.JavaConversions.asScalaBuffer
    JSON.readList(json, clazz)
  }

}
