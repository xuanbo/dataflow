package tk.fishfish.dataflow

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * 启动
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@SpringBootApplication
class Bootstrap {
}

object Bootstrap {

  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[Bootstrap], args: _*)
  }

}
