package tk.fishfish.dataflow

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableAsync
import tk.fishfish.mybatis.enums.EnableEnumTypes

/**
 * 启动
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@EnableAsync
@EnableEnumTypes
@SpringBootApplication
class Bootstrap {
}

object Bootstrap {

  def main(args: Array[String]): Unit = {
    try {
      SpringApplication.run(classOf[Bootstrap], args: _*)
    } catch {
      case e: Throwable => {
        println("启动异常", e)
      }
    }
  }

}
