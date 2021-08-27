package tk.fishfish.dataflow.database.sql

import tk.fishfish.dataflow.util.{JdbcUtils, Properties}

/**
 * 达梦实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class DmExecutor(override val props: Properties) extends RdbmsExecutor(props) {
}

class DmExecutorFactory extends ExecutorFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.dmPrefix)

  override def create(props: Properties): Executor = new DmExecutor(props)

}