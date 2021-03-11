package tk.fishfish.dataflow.database

import tk.fishfish.dataflow.exception.DatabaseMetadataException
import tk.fishfish.dataflow.util.{JdbcUtils, Properties}

import java.sql.{ResultSetMetaData, SQLException, Types}

/**
 * IoTDB实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class IotdbMetaDataQuery(props: Properties) extends SqlMetaDataQuery(props) {

  override def tables(): Seq[String] = {
    try {
      val con = getCon
      import scala.collection.JavaConversions.asScalaBuffer
      JdbcUtils.queryForList(con, "show storage group", classOf[String])
    } catch {
      case e: SQLException => throw new DatabaseMetadataException("根据SQL查询数据源表错误: " + e.getMessage, e)
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
      case e: SQLException => throw new DatabaseMetadataException("根据SQL查询数据源表错误: " + e.getMessage, e)
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

}
