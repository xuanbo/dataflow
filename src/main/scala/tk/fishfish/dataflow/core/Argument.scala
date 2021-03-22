package tk.fishfish.dataflow.core

import com.fasterxml.jackson.annotation.JsonInclude

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

  def getString(key: String): String = {
    val value = get(key)
    if (value == null) {
      return null
    }
    value.toString
  }

  def getInt(key: String): Int = {
    val value = get(key)
    if (value == null) {
      return 0
    }
    Integer.parseInt(value.toString)
  }

  def getSeq(key: String): Seq[Configuration] = {
    val value = get(key)
    if (value == null) {
      return Seq.empty
    }
    value match {
      case v: Seq[Map[String, Any]] => {
        v.map(map => {
          val conf = new Configuration()
          map.foreach(e => conf.put(e._1, e._2))
          conf
        })
      }
      case v => throw new IllegalArgumentException(s"参数类型不合法, 期待: Seq[Map[String, Any]], 实际: ${v.getClass}")
    }
  }

}
