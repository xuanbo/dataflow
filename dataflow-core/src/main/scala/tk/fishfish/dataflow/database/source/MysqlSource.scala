package tk.fishfish.dataflow.database.source

import com.mysql.cj.jdbc.Driver
import tk.fishfish.dataflow.util.{JdbcUtils, Properties}

/**
 * MySQL实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class MysqlSource(override val props: Properties) extends RdbmsSource(props) {

  /**
   * https://www.jianshu.com/p/c7c5dbe63019
   * 流式查询
   */
  override val fetchSize: Int = Int.MinValue

  override protected def driverClass(): String = classOf[Driver].getName

  override protected def quoteIdentifier(identifier: String): String = s"`$identifier`"

}

class MysqlSourceFactory extends SourceFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.mysqlPrefix)

  override def create(props: Properties): Source = new MysqlSource(props)

}
