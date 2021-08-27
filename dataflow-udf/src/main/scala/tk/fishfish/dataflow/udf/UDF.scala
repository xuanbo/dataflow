package tk.fishfish.dataflow.udf

import org.apache.spark.sql.expressions.UserDefinedFunction

/**
 * 自定义Spark SQL UDF
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait UDF extends Serializable {

  def name(): String

  def udf: UserDefinedFunction

}
