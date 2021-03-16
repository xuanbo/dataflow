package tk.fishfish.dataflow.database

import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.entity.enums.JdbcProperty
import tk.fishfish.dataflow.exception.DatabaseMetaDataException
import tk.fishfish.dataflow.util.{JdbcUtils, Properties, StringUtils, Validation}

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet, ResultSetMetaData, SQLException}
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
 * 通用SQL元数据查询
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class SqlMetaDataQuery(props: Properties) extends MetaDataQuery {

  protected val logger: Logger = LoggerFactory.getLogger(classOf[SqlMetaDataQuery])

  protected val url: String = {
    val url = props.getString(JdbcProperty.URL.key())
    Validation.notEmpty(url, "jdbc url参数不能为空")
    url
  }
  protected val user: String = props.getString(JdbcProperty.USER.key())
  protected val password: String = props.getString(JdbcProperty.PASSWORD.key())
  protected val catalog: String = props.getString(JdbcProperty.CATALOG.key())
  protected val schema: String = props.getString(JdbcProperty.SCHEMA.key())

  override def ping(): String = {
    var con: Connection = null
    try {
      con = getCon
      resetCatalogAndSchema(con)
      null
    } catch {
      case e: SQLException => e.getMessage
      case e: DatabaseMetaDataException => e.getMessage
    } finally {
      JdbcUtils.close(con)
    }
  }

  override def tables(): Seq[String] = {
    var tables = mutable.Seq[String]()
    var con: Connection = null
    var rs: ResultSet = null
    try {
      con = getCon
      val (catalog, schema) = resetCatalogAndSchema(con)
      val databaseMetaData = con.getMetaData
      rs = databaseMetaData.getTables(catalog, schema, "%", Array[String]("TABLE"))
      while (rs.next) {
        val name = rs.getString("TABLE_NAME")
        tables = tables :+ name
      }
    } catch {
      case e: SQLException => throw new DatabaseMetaDataException("查询数据源表错误: " + e.getMessage, e)
    } finally {
      JdbcUtils.close(rs)
      JdbcUtils.close(con)
    }
    tables
  }

  override def showTable(name: String): Table = {
    var catalog, schema = ""
    var columns = mutable.Seq[Column]()
    var con: Connection = null
    var rs: ResultSet = null
    try {
      con = getCon
      val tuple = resetCatalogAndSchema(con)
      catalog = tuple._1
      schema = tuple._2
      val databaseMetaData = con.getMetaData
      rs = databaseMetaData.getColumns(catalog, schema, name, "%")
      while (rs.next) {
        val metaData = rs.getMetaData
        val map = mutable.Map[String, Any]()
        for (i <- 1 to metaData.getColumnCount) {
          map += (rs.getMetaData.getColumnName(i) -> rs.getObject(i))
        }
        val column = extractColumn(map.toMap)
        columns = columns :+ column
      }
    } catch {
      case e: SQLException => throw new DatabaseMetaDataException("查询数据源表错误: " + e.getMessage, e)
    } finally {
      JdbcUtils.close(rs)
      JdbcUtils.close(con)
    }
    Table(name, catalog, schema, columns)
  }

  override def showSql(sql: String): Table = {
    var catalog, schema = ""
    var columns = mutable.Seq[Column]()
    var con: Connection = null
    var ps: PreparedStatement = null
    var rs: ResultSet = null
    try {
      con = getCon
      val tuple = resetCatalogAndSchema(con)
      catalog = tuple._1
      schema = tuple._2
      ps = con.prepareStatement(sql)
      rs = ps.executeQuery
      val metaData = rs.getMetaData
      for (i <- 1 to metaData.getColumnCount) {
        columns = columns :+ extractColumn(metaData, i)
      }
    } catch {
      case e: SQLException => throw new DatabaseMetaDataException("根据SQL查询数据源字段结构错误: " + e.getMessage, e)
    } finally {
      JdbcUtils.close(rs)
      JdbcUtils.close(ps)
      JdbcUtils.close(con)
    }
    Table(null, catalog, schema, columns)
  }

  protected def getCon: Connection = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Await.result(Future {
      DriverManager.getConnection(url, user, password)
    }, 5 second)
  }

  protected def extractColumn(metaData: Map[String, Any]): Column = Column(
    metaData.get("COLUMN_NAME").map(_.asInstanceOf[String]).orNull,
    metaData.get("TYPE_NAME").map(_.asInstanceOf[String]).orNull,
    metaData.get("COLUMN_SIZE").map(_.asInstanceOf[Int]).getOrElse(0),
    metaData.get("DECIMAL_DIGITS").map(_.asInstanceOf[Int]).getOrElse(0),
    "YES".equals(metaData.get("IS_NULLABLE").map(_.asInstanceOf[String]).orNull),
    metaData.get("REMARKS").map(_.asInstanceOf[String]).orNull
  )

  protected def extractColumn(metaData: ResultSetMetaData, index: Int): Column = Column(
    metaData.getColumnLabel(index),
    metaData.getColumnTypeName(index),
    metaData.getColumnDisplaySize(index),
    metaData.getScale(index),
    metaData.isNullable(index) == 1,
    null
  )

  protected def resetCatalogAndSchema(con: Connection): (String, String) = {
    var catalog = this.catalog
    var schema = this.schema
    try {
      catalog = if (StringUtils.isEmpty(this.catalog)) {
        con.getCatalog
      } else {
        con.setCatalog(this.catalog)
        this.catalog
      }
      schema = if (StringUtils.isEmpty(this.schema)) {
        con.getSchema
      } else {
        con.setSchema(this.schema)
        this.schema
      }
    } catch {
      case e: SQLException => logger.warn("设置catalog、schema错误: {}", e.getMessage)
    }
    (catalog, schema)
  }

}
