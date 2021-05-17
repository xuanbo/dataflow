package tk.fishfish.dataflow.sink

import tk.fishfish.dataflow.entity.enums.JdbcProperty
import tk.fishfish.dataflow.util.{Properties, Validation}
import tk.fishfish.rest.execption.BizException

/**
 * 目标端工厂
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object SinkFactory {

  def create(props: Properties): SqlSink = {
    val url = props.getString(JdbcProperty.URL.key())
    Validation.nonEmpty(url, "jdbc url参数不能为空")
    if (url.startsWith("jdbc:mysql://")) {
      new MysqlSink(props)
    } else if (url.startsWith("jdbc:iotdb://")) {
      new IotdbSink(props)
    } else {
      throw BizException.of(500, s"不支持的驱动: $url，请联系管理员适配")
    }
  }

}
