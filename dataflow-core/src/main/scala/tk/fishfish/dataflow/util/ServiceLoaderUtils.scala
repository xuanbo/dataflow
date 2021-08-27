package tk.fishfish.dataflow.util

import java.util.ServiceLoader
import scala.collection.mutable.ListBuffer

/**
 * SPI加载工具类
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object ServiceLoaderUtils {

  def load[T](service: Class[T]): Seq[T] = {
    val buf = new ListBuffer[T]()
    import scala.collection.JavaConversions.asScalaIterator
    ServiceLoader.load(service, service.getClassLoader).iterator().foreach { e =>
      buf += e
    }
    buf
  }

}
