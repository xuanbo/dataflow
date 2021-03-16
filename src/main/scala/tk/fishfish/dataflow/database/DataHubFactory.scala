package tk.fishfish.dataflow.database

import tk.fishfish.dataflow.database.driver.{IotdbDataHub, MysqlDataHub}
import tk.fishfish.dataflow.entity.enums.{DriverType, JdbcProperty}
import tk.fishfish.dataflow.util.{Properties, Validation}
import tk.fishfish.rest.BizException

/**
 * 数据管理工厂
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object DataHubFactory {

  def create(driverType: DriverType, props: Properties): DataHub = {
    driverType match {
      case DriverType.MYSQL => new MysqlDataHub(props)
      case DriverType.IOT => new IotdbDataHub(props)
      case _ => throw BizException.of(500, s"不支持的驱动: $driverType，请联系管理员适配")
    }
  }

  def create(props: Properties): DataHub = {
    val url = props.getString(JdbcProperty.URL.key())
    Validation.notEmpty(url, "jdbc url参数不能为空")
    if (url.startsWith("jdbc:mysql://")) {
      new MysqlDataHub(props)
    } else if (url.startsWith("jdbc:iotdb://")) {
      new IotdbDataHub(props)
    } else {
      throw BizException.of(500, s"不支持的驱动: $url，请联系管理员适配")
    }
  }

}
