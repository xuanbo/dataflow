package tk.fishfish.dataflow.database.sql

import tk.fishfish.dataflow.util.{JdbcUtils, Properties}

/**
 * MySQL实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class MysqlExecutor(override val props: Properties) extends RdbmsExecutor(props) {
}

class MysqlExecutorFactory extends ExecutorFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.mysqlPrefix)

  override def create(props: Properties): Executor = new MysqlExecutor(props)

}