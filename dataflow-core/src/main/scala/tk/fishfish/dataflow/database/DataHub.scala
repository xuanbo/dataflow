package tk.fishfish.dataflow.database

import org.apache.spark.sql.Row

import java.sql.SQLException

/**
 * 数据管理
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait DataHub {

  @throws[SQLException]
  def insert(table: String, columns: Seq[String], rows: Iterator[Row]): Unit

  @throws[SQLException]
  def update(table: String, columns: Seq[String], rows: Iterator[Row]): Unit

}
