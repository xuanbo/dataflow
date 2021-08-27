package tk.fishfish.dataflow.database.sink

import com.mysql.cj.conf.PropertyKey
import tk.fishfish.dataflow.util.{JdbcOptions, JdbcUtils, Properties, Validation}

/**
 * MySQL实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class MysqlSink(override val props: Properties) extends RdbmsSink(props) {

  override val url: String = {
    var url = props.getString(JdbcOptions.URL)
    Validation.nonEmpty(url, "jdbc url参数不能为空")
    // 超时连接参数
    url = addUrlParams(url, PropertyKey.connectTimeout.getKeyName, "3000")
    url = addUrlParams(url, PropertyKey.socketTimeout.getKeyName, "60000")
    // MySQL JDBC 驱动在默认情况下会无视 executeBatch() 语句，把我们期望批量执行的一组sql语句拆散，一条一条地发给 MySQL 数据库。
    // 批量插入实际上是单条插入，直接造成较低的性能。
    // 只有把 rewriteBatchedStatements 参数置为 true, 驱动才会帮你批量执行 SQL
    // 另外这个选项对 INSERT/UPDATE/DELETE 都有效
    url = addUrlParams(url, PropertyKey.rewriteBatchedStatements.getKeyName, "true")
    url
  }

  private def addUrlParams(url: String, param: String, value: String): String = {
    if (!url.contains(param)) {
      if (url.contains("?")) {
        s"$url&$param=$value"
      } else {
        s"$url?$param=$value"
      }
    } else {
      url
    }
  }

  override protected def insertSql(table: String, columns: Seq[String]): String =
    s"""
       |INSERT INTO ${quoteIdentifier(table)} ${columns.map(quoteIdentifier).mkString("(", ", ", ")")}
       |VALUES ${columns.map(_ => "?").mkString("(", ", ", ")")}
       |""".stripMargin.replaceAll("\n", " ")

  override protected def updateSql(table: String, columns: Seq[String]): String =
    s"""
       |INSERT INTO ${quoteIdentifier(table)} ${columns.map(quoteIdentifier).mkString("(", ", ", ")")}
       |VALUES ${columns.map(_ => "?").mkString("(", ", ", ")")}
       |ON DUPLICATE KEY UPDATE ${columns.map(quoteIdentifier).map(e => s"$e = $e").mkString(", ")}
       |""".stripMargin.replaceAll("\n", " ")

  override protected def quoteIdentifier(identifier: String): String = s"`$identifier`"

}

class MysqlSinkFactory extends SinkFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.mysqlPrefix)

  override def create(props: Properties): Sink = new MysqlSink(props)

}
