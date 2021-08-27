package tk.fishfish.dataflow.util

import scala.annotation.tailrec

/**
 * 字符串模板
 *
 * @see [[https://stackoverflow.com/questions/6110062/simple-string-template-replacement-in-scala-and-clojure]]
 * @author 奔波儿灞
 * @version 1.0.0
 */
object StringTemplate {

  /**
   * Replace templates of the form {key} in the input String with values from the Map.
   *
   * @param text      the String in which to do the replacements
   * @param variables a Map from Symbol (key) to value
   * @return the String with all occurrences of the templates replaced by their values
   */
  def render(text: String, variables: Map[String, Any]): String = {
    val builder = new StringBuilder

    @tailrec
    def loop(text: String): String = {
      if (text.isEmpty) {
        builder.toString
      } else if (text.startsWith("{")) {
        val brace = text.indexOf("}")
        if (brace < 0) {
          builder.append(text).toString
        } else {
          val replacement = variables.get(text.substring(1, brace)).orNull
          if (replacement != null) {
            builder.append(replacement)
            loop(text.substring(brace + 1))
          } else {
            builder.append("{")
            loop(text.substring(1))
          }
        }
      } else {
        val brace = text.indexOf("{")
        if (brace < 0) {
          builder.append(text).toString
        } else {
          builder.append(text.substring(0, brace))
          loop(text.substring(brace))
        }
      }
    }

    loop(text)
  }

}
