package tk.fishfish.dataflow.database

import org.apache.spark.sql.Row
import tk.fishfish.dataflow.entity.enums.JdbcProperty
import tk.fishfish.dataflow.exception.DatabaseUpdateException
import tk.fishfish.dataflow.util.{CollectionUtils, JdbcUtils, Properties}

import java.sql.{Connection, PreparedStatement, SQLException}

/**
 * 通用SQL数据管理
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
abstract class SqlDataHub(props: Properties) extends SqlMetaDataQuery(props) with DataHub {

  protected val batch: Int = props.getInt(JdbcProperty.BATCH.key(), 200)

  override def insert(table: String, columns: Seq[String], rows: Iterator[Row]): Unit = {
    if (CollectionUtils.isEmpty(rows)) {
      return
    }
    val sql = s"""insert into $table ${columns.mkString("(", ", ", ")")} values ${columns.map(_ => "?").mkString("(", ", ", ")")}"""
    logger.info("执行SQL: {}", sql)
    var con: Connection = null
    var ps: PreparedStatement = null
    try {
      con = getCon
      resetCatalogAndSchema(con)
      con.setAutoCommit(false)
      ps = con.prepareStatement(sql)
      var batch = 0
      for (row <- rows) {
        batch = batch + 1
        var i = 0
        for (column <- columns) {
          val value: Any = row.getAs(column)
          ps.setObject(i + 1, value)
          i = i + 1
        }
        ps.addBatch()
        if (batch == this.batch) {
          ps.executeBatch()
          con.commit()
          batch = 0
        }
      }
      ps.executeBatch()
      con.commit()
    } catch {
      case e: SQLException => throw new DatabaseUpdateException("写入数据库表错误: " + e.getMessage, e)
    } finally {
      JdbcUtils.close(ps)
      JdbcUtils.close(con)
    }
  }

}
