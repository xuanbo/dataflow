package tk.fishfish.dataflow.exception

/**
 * DAG异常
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class DagException(message: String, cause: Throwable) extends RuntimeException(message, cause) {

  def this(message: String) {
    this(message, null)
  }

}
