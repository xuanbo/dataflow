package tk.fishfish.dataflow.database.source

import com.microsoft.sqlserver.jdbc.SQLServerDriver
import tk.fishfish.dataflow.util.{JdbcUtils, Properties}

/**
 * SqlServer实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class SqlServerSource(override val props: Properties) extends RdbmsSource(props) {

  override protected def driverClass(): String = classOf[SQLServerDriver].getName

  override protected def quoteIdentifier(identifier: String): String =
    s"""
       |"$identifier"
       |""".stripMargin.replaceAll("\n", "")

}

class SqlServerSourceFactory extends SourceFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.sqlserverPrefix)

  override def create(props: Properties): Source = new SqlServerSource(props)

}
