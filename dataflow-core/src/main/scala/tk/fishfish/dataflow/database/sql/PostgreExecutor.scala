package tk.fishfish.dataflow.database.sql

import tk.fishfish.dataflow.util.{JdbcUtils, Properties}

/**
 * PostgreSQL实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class PostgreExecutor(override val props: Properties) extends RdbmsExecutor(props) {
}

class PostgreExecutorFactory extends ExecutorFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.postgrePrefix)

  override def create(props: Properties): Executor = new PostgreExecutor(props)

}