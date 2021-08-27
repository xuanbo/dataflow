package tk.fishfish.dataflow.database.sink

import tk.fishfish.dataflow.exception.DatabaseException
import tk.fishfish.dataflow.util.{CollectionUtils, JdbcUtils, Properties}

/**
 * Oracle实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class OracleSink(override val props: Properties) extends RdbmsSink(props) {

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
       |MERGE INTO ${quoteIdentifier(table)} A
       |USING (SELECT ${columns.map(e => s"? AS ${quoteIdentifier(e)}").mkString(", ")} FROM DUAL) TMP
       |ON (${pks.map(quoteIdentifier).map(e => s"A.$e = TMP.$e").mkString(" AND ")})
       |WHEN MATCHED THEN
       |UPDATE SET ${columns.filterNot(pks.contains(_)).map(quoteIdentifier).map(e => s"$e = TMP.$e").mkString(", ")}
       |WHEN NOT MATCHED THEN
       |INSERT(${columns.map(quoteIdentifier).mkString(", ")}) VALUES(${columns.map(e => s"TMP.${quoteIdentifier(e)}").mkString(", ")})
       |""".stripMargin.replaceAll("\n", " ")
  }

  override protected def quoteIdentifier(identifier: String): String =
    s"""
       |"$identifier"
       |""".stripMargin.replaceAll("\n", "")

  private def getPks(table: String): Seq[String] = JdbcUtils.query(
    getCon,
    s"""
       |SELECT CU.COLUMN_NAME PK FROM USER_CONS_COLUMNS CU, USER_CONSTRAINTS AU
       |WHERE CU.CONSTRAINT_NAME = AU.CONSTRAINT_NAME AND
       |AU.CONSTRAINT_TYPE = 'P' AND CU.TABLE_NAME = '$table'
       |""".stripMargin.replaceAll("\n", " "),
    JdbcUtils.stringColumnResultSetExtractor("PK")
  )

}

class OracleSinkFactory extends SinkFactory {

  override def accept(url: String): Boolean = url.startsWith(JdbcUtils.oraclePrefix)

  override def create(props: Properties): Sink = new OracleSink(props)

}
