package tk.fishfish.dataflow.core

import com.fasterxml.jackson.annotation.JsonInclude
import tk.fishfish.json.util.JSON

import scala.beans.BeanProperty

/**
 * 配置
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class Argument {

  @BeanProperty
  var namespace: String = _

  @BeanProperty
  var input: Configuration = _

  @BeanProperty
  var output: Configuration = _

  @BeanProperty
  var tables: Seq[String] = _

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
