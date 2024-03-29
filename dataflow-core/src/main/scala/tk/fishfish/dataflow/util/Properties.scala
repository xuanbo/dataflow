package tk.fishfish.dataflow.util

import scala.collection.mutable

/**
 * 配置
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class Properties extends mutable.LinkedHashMap[String, Any] {

  def getString(key: String): String = getString(key, null)

  def getString(key: String, default: String): String = get(key) match {
    case Some(e) => e match {
      case null => default
      case str: String => str
      case _ => e.toString
    }
    case None => default
  }

  def getInt(key: String): Int = getInt(key, 0)

  def getInt(key: String, default: Int): Int = get(key) match {
    case Some(e) => e match {
      case null => default
      case int: Int => int
      case _ => Integer.parseInt(e.toString)
    }
    case None => default
  }

  def option(key: String, value: Any): Properties = {
    put(key, value)
    this
  }

}
