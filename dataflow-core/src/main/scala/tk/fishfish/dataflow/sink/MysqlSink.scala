package tk.fishfish.dataflow.sink

import com.mysql.cj.conf.PropertyKey
import tk.fishfish.dataflow.util.{Properties, Validation}

/**
 * MySQL实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class MysqlSink(val props: Properties) extends SqlSink(props) {

  override protected val url: String = {
    var url = props.getString("url")
    Validation.nonEmpty(url, "jdbc url参数不能为空")
    // 超时连接参数
    url = addUrlParams(url, PropertyKey.connectTimeout.getKeyName, "3000")
    url = addUrlParams(url, PropertyKey.socketTimeout.getKeyName, "60000")
    // MySQL JDBC驱动在默认情况下会无视 executeBatch() 语句，把我们期望批量执行的一组sql语句拆散，一条一条地发给MySQL数据库，批量插入实际上是单条插入，直接造成较低的性能。
    // 只有把rewriteBatchedStatements参数置为true, 驱动才会帮你批量执行SQL
    // 另外这个选项对INSERT/UPDATE/DELETE都有效
    url = addUrlParams(url, PropertyKey.rewriteBatchedStatements.getKeyName, "true")
    url
  }

  private[this] def addUrlParams(url: String, param: String, value: String): String = {
    if (!url.contains(param)) {
      if (url.contains("?")) {
        s"$url&$param=$value"
      } else {
        s"$url?$param=$value"
      }
    } else {
      url
    }
  }

}
