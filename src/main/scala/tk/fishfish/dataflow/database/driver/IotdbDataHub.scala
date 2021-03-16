package tk.fishfish.dataflow.database.driver

import org.apache.spark.sql.Row
import tk.fishfish.dataflow.database.{Column, SqlDataHub, Table}
import tk.fishfish.dataflow.exception.{DatabaseMetaDataException, DatabaseUpdateException}
import tk.fishfish.dataflow.util.{CollectionUtils, JdbcUtils, Properties}

import java.sql.{Connection, PreparedStatement, ResultSetMetaData, SQLException, Types}

/**
 * IoTDB实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class IotdbDataHub(val props: Properties) extends SqlDataHub(props) {

  override def tables(): Seq[String] = {
    try {
      val con = getCon
      import scala.collection.JavaConversions.asScalaBuffer
      JdbcUtils.queryForList(con, "show storage group", classOf[String])
    } catch {
      case e: SQLException => throw new DatabaseMetaDataException("根据SQL查询数据源表错误: " + e.getMessage, e)
    }
  }

  override def showTable(name: String): Table = {
    try {
      val con = getCon
      import scala.collection.JavaConversions.asScalaBuffer
      var columns = JdbcUtils.queryForList(con, s"show timeseries $name").map { e =>
        val name = e.get("timeseries").toString.replace(s"${e.get("storage group").toString}.", "")
        Column(name, e.get("dataType").toString, -1, -1, isNullable = true, null)
      }
      columns = Column("Time", "TIME", -1, -1, isNullable = false, "时间序列") +: columns
      Table(name, null, null, columns)
    } catch {
      case e: SQLException => throw new DatabaseMetaDataException("根据SQL查询数据源表错误: " + e.getMessage, e)
    }
  }

  override protected def extractColumn(metaData: ResultSetMetaData, index: Int): Column = {
    val name = metaData.getColumnLabel(index).split("\\.").last
    val typeName = metaData.getColumnType(index) match {
      case Types.TIMESTAMP => "TIMESTAMP"
      case Types.BOOLEAN => "BOOLEAN"
      case Types.INTEGER => "INT32"
      case Types.BIGINT => "INT64"
      case Types.FLOAT => "FLOAT"
      case Types.DOUBLE => "DOUBLE"
      case Types.VARCHAR => "TEXT"
      case _ => "NULL"
    }
    val isNullable = !"Time".equals(name)
    Column(name, typeName, -1, -1, isNullable, null)
  }

  override def insert(table: String, columns: Seq[String], rows: Iterator[Row]): Unit = {
    if (CollectionUtils.isEmpty(rows)) {
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
    val sql = s"""insert into $table ${names.mkString("(", ", ", ")")} values ${names.map(_ => "?").mkString("(", ", ", ")")}"""
    logger.info("执行SQL: {}", sql)
    var con: Connection = null
    var ps: PreparedStatement = null
    try {
      con = getCon
      resetCatalogAndSchema(con)
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
    } catch {
      case e: SQLException => throw new DatabaseUpdateException("写入数据库表错误: " + e.getMessage, e)
    } finally {
      JdbcUtils.close(ps)
      JdbcUtils.close(con)
    }
  }

}
