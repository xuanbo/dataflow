package tk.fishfish.dataflow.database.source

import dm.jdbc.driver.DmDriver
import tk.fishfish.dataflow.util.{JdbcUtils, Properties}

/**
 * 达梦实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class DmSource(override val props: Properties) extends RdbmsSource(props) {

  override protected def driverClass(): String = classOf[DmDriver].getName

  override protected def quoteIdentifier(identifier: String): String =
    s"""
       |"$identifier"
       |""".stripMargin.replaceAll("\n", "")

}

class DmSourceFactory extends SourceFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.dmPrefix)

  override def create(props: Properties): Source = new DmSource(props)

}