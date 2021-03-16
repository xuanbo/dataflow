package tk.fishfish.dataflow.database

import org.junit.{Before, Test}
import tk.fishfish.dataflow.util.Properties

/**
 * 元数据查询测试
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class MetadataQueryTest {

  private var metaDataQuery: MetaDataQuery = _

  @Before
  def setup(): Unit = {
    val props = new Properties()
    props.option("url", "jdbc:iotdb://127.0.0.1:6667/")
    props.option("user", "root")
    props.option("password", "root")
    metaDataQuery = DataHubFactory.create(props)
  }

  @Test
  def ping(): Unit = {
    val msg = metaDataQuery.ping()
    println(msg)
  }

  @Test
  def tables(): Unit = {
    val tables = metaDataQuery.tables()
    for (table <- tables) {
      println(table)
    }
  }

  @Test
  def showTable(): Unit = {
    val table = metaDataQuery.showTable("root.demo")
    println(table)
  }

  @Test
  def showSql(): Unit = {
    val table = metaDataQuery.showSql("select * from root.demo")
    println(table)
  }

}
