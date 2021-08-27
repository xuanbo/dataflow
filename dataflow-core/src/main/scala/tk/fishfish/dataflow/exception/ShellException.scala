package tk.fishfish.dataflow.exception

/**
 * Shell异常
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class ShellException(message: String, cause: Throwable) extends RuntimeException(message, cause) {

  def this(message: String) {
    this(message, null)
  }

}
