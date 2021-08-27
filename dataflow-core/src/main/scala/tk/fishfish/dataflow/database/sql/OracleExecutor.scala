package tk.fishfish.dataflow.database.sql

import tk.fishfish.dataflow.util.{JdbcUtils, Properties}

/**
 * Oracle实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class OracleExecutor(override val props: Properties) extends RdbmsExecutor(props) {
}

class OracleExecutorFactory extends ExecutorFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.oraclePrefix)

  override def create(props: Properties): Executor = new OracleExecutor(props)

}