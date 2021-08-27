package tk.fishfish.dataflow.database.source

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.elasticsearch.hadoop.cfg.ConfigurationOptions
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.util.{EsUtils, JdbcOptions, Properties, SparkUtils}

import java.net.URI

/**
 * ElasticSearch实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class EsSource(val props: Properties) extends Source {

  private val logger: Logger = LoggerFactory.getLogger(classOf[EsSource])

  override def read(spark: SparkSession): DataFrame = {
    val url = props.getString(JdbcOptions.URL)
    val user = props.getString(JdbcOptions.USER)
    val password = props.getString(JdbcOptions.PASSWORD)
    val fetchSize = props.getInt(JdbcOptions.FETCH_SIZE, 10000)

    val endpoint = if (url.startsWith(EsUtils.esUrlPrefix)) {
      url.substring(EsUtils.esUrlPrefix.length)
    } else if (url.startsWith(EsUtils.opendistroUrlPrefix)) {
      url.substring(EsUtils.opendistroUrlPrefix.length)
    } else {
      throw new UnsupportedOperationException(s"不支持的ES数据源: $url")
    }
    val uri = URI.create(endpoint)

    var table = props.getString(JdbcOptions.TABLE)
    var query: String = null
    if (StringUtils.isBlank(table)) {
      val sql = props.getString(JdbcOptions.SQL)
      if (StringUtils.isBlank(sql)) {
        throw new IllegalArgumentException("配置 [argument.input.table] [argument.input.sql] 不能同时为空")
      }
      logger.info("查询SQL: {}", sql)
      val tables = SparkUtils.parseTables(spark, sql)
      table = tables(0)
      query = EsUtils.translate(endpoint, sql, user, password)
    }
    logger.info(s"查询索引: $table, 条件: $query")

    spark.read.format("org.elasticsearch.spark.sql")
      .option(ConfigurationOptions.ES_NODES, uri.getHost)
      .option(ConfigurationOptions.ES_PORT, uri.getPort)
      .option(ConfigurationOptions.ES_NET_HTTP_AUTH_USER, user)
      .option(ConfigurationOptions.ES_NET_HTTP_AUTH_PASS, password)
      .option(ConfigurationOptions.ES_RESOURCE_READ, table)
      .option(ConfigurationOptions.ES_QUERY, query)
      .option(ConfigurationOptions.ES_HTTP_TIMEOUT, "10s")
      .option(ConfigurationOptions.ES_SCROLL_SIZE, fetchSize)
      .option(ConfigurationOptions.ES_NODES_WAN_ONLY, value = true)
      .load()
  }

}

class EsSourceFactory extends SourceFactory {

  override def accept(url: String): Boolean =
    url.startsWith(EsUtils.esUrlPrefix) || url.startsWith(EsUtils.opendistroUrlPrefix)

  override def create(props: Properties): Source = new EsSource(props)

}
