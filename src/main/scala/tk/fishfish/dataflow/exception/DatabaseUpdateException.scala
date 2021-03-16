package tk.fishfish.dataflow.exception

/**
 * 更新操作异常
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class DatabaseUpdateException(message: String, cause: Throwable) extends RuntimeException(message, cause) {

  def this(message: String) {
    this(message, null)
  }

}
