package tk.fishfish.dataflow.database.driver

import com.mysql.cj.conf.PropertyKey
import tk.fishfish.dataflow.database.SqlDataHub
import tk.fishfish.dataflow.util.{Properties, Validation}

/**
 * MySQL实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class MysqlDataHub(val props: Properties) extends SqlDataHub(props) {

  override protected val url: String = {
    var url = props.getString("url")
    Validation.notEmpty(url, "jdbc url参数不能为空")
    // 超时连接参数
    if (!url.contains(PropertyKey.connectTimeout.getKeyName)) {
      url = if (url.contains("?")) {
        s"$url&${PropertyKey.connectTimeout.getKeyName}=3000"
      } else {
        s"$url?${PropertyKey.connectTimeout.getKeyName}=3000"
      }
    }
    if (!url.contains(PropertyKey.socketTimeout.getKeyName)) {
      url = if (url.contains("?")) {
        s"$url&${PropertyKey.socketTimeout.getKeyName}=60000"
      } else {
        s"$url?${PropertyKey.socketTimeout.getKeyName}=60000"
      }
    }
    url
  }

}
