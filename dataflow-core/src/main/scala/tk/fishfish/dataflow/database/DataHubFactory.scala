package tk.fishfish.dataflow.database

import tk.fishfish.dataflow.database.driver.{IotdbDataHub, MysqlDataHub}
import tk.fishfish.dataflow.entity.enums.JdbcProperty
import tk.fishfish.dataflow.util.{Properties, Validation}
import tk.fishfish.rest.BizException

/**
 * 数据管理工厂
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object DataHubFactory {

  def create(props: Properties): DataHub = {
    val url = props.getString(JdbcProperty.URL.key())
    Validation.nonEmpty(url, "jdbc url参数不能为空")
    if (url.startsWith("jdbc:mysql://")) {
      new MysqlDataHub(props)
    } else if (url.startsWith("jdbc:iotdb://")) {
      new IotdbDataHub(props)
    } else {
      throw BizException.of(500, s"不支持的驱动: $url，请联系管理员适配")
    }
  }

}
