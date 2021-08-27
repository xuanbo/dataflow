package tk.fishfish.dataflow.database.sink

import tk.fishfish.dataflow.util.{JdbcUtils, Properties}

/**
 * 达梦实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class DmSink(override val props: Properties) extends OracleSink(props) {
}

class DmSinkFactory extends SinkFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.dmPrefix)

  override def create(props: Properties): Sink = new DmSink(props)

}