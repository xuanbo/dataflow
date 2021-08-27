package tk.fishfish.dataflow.exception

/**
 * UDF异常
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class UDFException(message: String, cause: Throwable) extends RuntimeException(message, cause) {

  def this(message: String) {
    this(message, null)
  }

}
