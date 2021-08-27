package tk.fishfish.dataflow.database.sink

import org.apache.spark.sql.DataFrame
import tk.fishfish.dataflow.util.Properties

/**
 * 目标端
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Sink extends Serializable {

  def write(df: DataFrame): Unit

}

trait SinkFactory extends Serializable {

  def accept(url: String): Boolean

  def create(props: Properties): Sink

}
