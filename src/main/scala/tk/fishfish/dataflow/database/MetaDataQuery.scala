package tk.fishfish.dataflow.database

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 元数据查询
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait MetaDataQuery {

  def ping(): String

  def tables(): Seq[String]

  def showTable(name: String): Table

  def showSql(sql: String): Table

}

case class Table(name: String, catalog: String, schema: String, columns: Seq[Column])

case class Column(name: String, @JsonProperty("type") colType: String, length: Int, decimal: Int, isNullable: Boolean, comment: String)
