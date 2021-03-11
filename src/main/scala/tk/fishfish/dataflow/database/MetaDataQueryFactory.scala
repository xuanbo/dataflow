package tk.fishfish.dataflow.database

import org.apache.iotdb.jdbc.IoTDBDriver
import tk.fishfish.dataflow.entity.enums.DriverType
import tk.fishfish.dataflow.util.Properties
import tk.fishfish.rest.BizException

/**
 * 元数据查询工厂
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
object MetaDataQueryFactory {

  def create(driverType: DriverType, props: Properties): MetaDataQuery = {
    Class.forName(classOf[IoTDBDriver].getName)
    driverType match {
      case DriverType.MYSQL => new MysqlMetaDataQuery(props)
      case DriverType.IOT => new IotdbMetaDataQuery(props)
      case _ => throw BizException.of(500, s"不支持的驱动类型: $driverType，请联系管理员适配")
    }
  }

}
