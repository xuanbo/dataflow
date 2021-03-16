package tk.fishfish.dataflow.database

import org.apache.spark.sql.Row

/**
 * 数据管理
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait DataHub extends MetaDataQuery{

  def insert(table: String, columns: Seq[String], rows: Iterator[Row]): Unit

}
