package tk.fishfish.dataflow.sink

import org.apache.spark.sql.Row

import java.sql.SQLException

/**
 * 目标端
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Sink {

  @throws[SQLException]
  def insert(table: String, columns: Seq[String], rows: Iterator[Row]): Unit

  @throws[SQLException]
  def update(table: String, columns: Seq[String], rows: Iterator[Row]): Unit

}
