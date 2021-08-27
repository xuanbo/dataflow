package tk.fishfish.dataflow.database.source

import org.postgresql.Driver
import tk.fishfish.dataflow.util.{JdbcUtils, Properties}

/**
 * PostgreSQL实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class PostgreSource(override val props: Properties) extends RdbmsSource(props) {

  override protected def driverClass(): String = classOf[Driver].getName

  override protected def quoteIdentifier(identifier: String): String =
    s"""
       |"$identifier"
       |""".stripMargin.replaceAll("\n", "")

}

class PostgreSourceFactory extends SourceFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.postgrePrefix)

  override def create(props: Properties): Source = new PostgreSource(props)

}
