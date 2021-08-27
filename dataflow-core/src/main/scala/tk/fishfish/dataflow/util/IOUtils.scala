package tk.fishfish.dataflow.util

/**
 * I/O工具
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object IOUtils {

  def using[T <: AutoCloseable, R](closeable: T)(f: T => R): R = {
    try {
      f(closeable)
    } finally {
      closeable.close()
    }
  }

}
