package tk.fishfish.dataflow.config

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.datatype.guava.GuavaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.springframework.context.annotation.{Bean, Configuration}

/**
 * 配置jackson
 *
 * @author 奔波儿灞
 * @since 1.0.0
 */
@Configuration
class JacksonConfiguration {

  /**
   * 注册scala模块
   *
   * @return [[DefaultScalaModule]]
   */
  @Bean def scalaModule: Module = DefaultScalaModule

  /**
   * 注册guava模块
   *
   * @return [[GuavaModule]]
   */
  @Bean def guavaModule: Module = new GuavaModule

}
