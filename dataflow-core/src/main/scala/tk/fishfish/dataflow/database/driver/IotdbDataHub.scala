package tk.fishfish.dataflow.database.driver

import org.apache.spark.sql.Row
import org.springframework.jdbc.support.JdbcUtils
import tk.fishfish.dataflow.database.SqlDataHub
import tk.fishfish.dataflow.util.{CollectionUtils, Properties}

import java.sql.{Connection, PreparedStatement}

/**
 * IoTDB实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class IotdbDataHub(val props: Properties) extends SqlDataHub(props) {

  override def insert(table: String, columns: Seq[String], rows: Iterator[Row]): Unit = {
    if (CollectionUtils.isEmpty(rows) || CollectionUtils.isEmpty(columns)) {
      return
    }
    // 时间字段放最前面
    var names = columns.sortWith { (left, right) =>
      if ("Time".equals(left)) {
        true
      } else if ("Time".equals(right)) {
        false
      } else {
        left.compareTo(right) > 0
      }
    }
    var hasTime = true
    if (!columns.contains("Time")) {
      names = "Time" +: names
      hasTime = false
    }
    val sql = s"""INSERT INTO $table ${names.mkString("(", ", ", ")")} VALUES ${names.map(_ => "?").mkString("(", ", ", ")")}"""
    logger.info("执行SQL: {}", sql)
    var con: Connection = null
    var ps: PreparedStatement = null
    try {
      con = getCon
      con.setAutoCommit(true)
      ps = con.prepareStatement(sql)
      for (row <- rows) {
        var i = 0
        for (column <- names) {
          val value: Any = if ("Time".equals(column)) {
            if (hasTime) {
              row.getAs(column)
            } else {
              System.currentTimeMillis()
            }
          } else {
            row.getAs(column)
          }
          ps.setObject(i + 1, value)
          i = i + 1
        }
        ps.executeUpdate()
      }
    } finally {
      JdbcUtils.closeStatement(ps)
      JdbcUtils.closeConnection(con)
    }
  }

  override def update(table: String, columns: Seq[String], rows: Iterator[Row]): Unit = this.insert(table, columns, rows)

}
