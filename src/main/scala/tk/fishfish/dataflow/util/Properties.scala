package tk.fishfish.dataflow.util

import scala.collection.mutable

/**
 * 配置
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class Properties extends mutable.LinkedHashMap[String, Any] {

  def getProperty(key: String): Any = get(key).orNull

  def getString(key: String): String = get(key) match {
    case Some(e) => e match {
      case str: String => str
      case null => null
      case _ => e.toString
    }
    case None => null
  }

  def option(key: String, value: Any): Properties = {
    put(key, value)
    this
  }

}
