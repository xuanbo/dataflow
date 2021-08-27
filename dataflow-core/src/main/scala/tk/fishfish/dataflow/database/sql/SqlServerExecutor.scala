package tk.fishfish.dataflow.database.sql

import tk.fishfish.dataflow.util.{JdbcUtils, Properties}

/**
 * SqlServer实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class SqlServerExecutor(override val props: Properties) extends RdbmsExecutor(props) {
}

class SqlServerExecutorFactory extends ExecutorFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.sqlserverPrefix)

  override def create(props: Properties): Executor = new SqlServerExecutor(props)

}