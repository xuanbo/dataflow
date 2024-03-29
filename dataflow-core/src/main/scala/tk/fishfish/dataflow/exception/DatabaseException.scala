package tk.fishfish.dataflow.exception

/**
 * 数据库异常
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class DatabaseException(message: String, cause: Throwable) extends RuntimeException(message, cause) {

  def this(message: String) {
    this(message, null)
  }

}
