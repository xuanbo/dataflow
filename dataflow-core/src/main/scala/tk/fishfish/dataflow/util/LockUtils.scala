package tk.fishfish.dataflow.util

import java.util.concurrent.locks.Lock

/**
 * 锁工具类
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object LockUtils {

  def using[T <: Lock, R](lock: T)(f: => R): R = {
    lock.lock()
    try {
      f
    } finally {
      lock.unlock()
    }
  }

}
