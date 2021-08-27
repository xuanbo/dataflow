package tk.fishfish.dataflow.database.sql

import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.util.{CollectionUtils, JdbcOptions, JdbcUtils, Properties, Validation}

/**
 * 关系型数据库SQL执行器
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class RdbmsExecutor(val props: Properties) extends Executor {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  protected val url: String = {
    val url = props.getString(JdbcOptions.URL)
    Validation.nonEmpty(url, "jdbc url参数不能为空")
    url
  }
  protected val user: String = props.getString(JdbcOptions.USER)
  protected val password: String = props.getString(JdbcOptions.PASSWORD)

  override def execute(sql: String*): Unit = {
    if (CollectionUtils.isEmpty(sql)) {
      return
    }
    JdbcUtils.using(JdbcUtils.getCon(url, user, password)) { con =>
      sql.foreach { e =>
        JdbcUtils.using(con.createStatement()) { st =>
          logger.info("执行SQL: {}", e)
          st.execute(e)
        }
      }
    }
  }

}
