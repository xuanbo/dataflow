package tk.fishfish.dataflow.database.source

import oracle.jdbc.driver.OracleDriver
import tk.fishfish.dataflow.util.{JdbcUtils, Properties}

/**
 * Oracle实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class OracleSource(override val props: Properties) extends RdbmsSource(props) {

  override protected def driverClass(): String = classOf[OracleDriver].getName

  override protected def quoteIdentifier(identifier: String): String =
    s"""
       |"$identifier"
       |""".stripMargin.replaceAll("\n", "")

}

class OracleSourceFactory extends SourceFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.oraclePrefix)

  override def create(props: Properties): Source = new OracleSource(props)

}
