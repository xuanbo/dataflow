package tk.fishfish.dataflow.database.sink

import tk.fishfish.dataflow.exception.DatabaseException
import tk.fishfish.dataflow.util.{CollectionUtils, JdbcUtils, Properties}

/**
 * PostgreSQL实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class PostgreSink(override val props: Properties) extends RdbmsSink(props) {

  override protected def insertSql(table: String, columns: Seq[String]): String =
    s"""
       |INSERT INTO ${quoteIdentifier(table)} ${columns.map(quoteIdentifier).mkString("(", ", ", ")")}
       |VALUES ${columns.map(_ => "?").mkString("(", ", ", ")")}
       |""".stripMargin.replaceAll("\n", " ")

  override protected def updateSql(table: String, columns: Seq[String]): String = {
    val pks = getPks(table)
    if (CollectionUtils.isEmpty(pks)) {
      throw new DatabaseException(s"表${table}无主键列")
    }
    s"""
       |INSERT INTO ${quoteIdentifier(table)} ${columns.map(quoteIdentifier).mkString(", ", "(", ")")}
       |VALUES(${columns.map(_ => "?").mkString(", ")})
       |ON CONFLICT (${pks.map(quoteIdentifier).mkString(", ")}) DO
       |UPDATE SET ${columns.filterNot(pks.contains(_)).map(quoteIdentifier).map(e => s"$e = EXCLUDED.$e").mkString(", ")}
       |""".stripMargin.replaceAll("\n", " ")
  }

  override protected def quoteIdentifier(identifier: String): String =
    s"""
       |"$identifier"
       |""".stripMargin.replaceAll("\n", "")

  private def getPks(table: String): Seq[String] = JdbcUtils.query(
    getCon,
    s"""
       |SELECT PG_CONSTRAINT.CONNAME AS PK FROM PG_CONSTRAINT
       |INNER JOIN PG_CLASS ON PG_CONSTRAINT.CONRELID = PG_CLASS.OID
       |WHERE PG_CLASS.RELNAME = '$table' AND PG_CONSTRAINT.CONTYPE = 'p'
       |""".stripMargin.replaceAll("\n", " "),
    JdbcUtils.stringColumnResultSetExtractor("PK")
  )

}

class PostgreSinkFactory extends SinkFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.postgrePrefix)

  override def create(props: Properties): Sink = new OracleSink(props)

}
