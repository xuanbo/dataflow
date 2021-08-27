package tk.fishfish.dataflow.database.source

import org.apache.spark.sql.{DataFrame, SparkSession}
import tk.fishfish.dataflow.util.Properties

/**
 * 源端
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Source extends Serializable {

  def read(spark: SparkSession): DataFrame

}

trait SourceFactory extends Serializable {

  def accept(url: String): Boolean

  def create(props: Properties): Source

}
