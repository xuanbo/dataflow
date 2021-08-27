package tk.fishfish.dataflow.database.sink

import org.apache.spark.sql.{DataFrame, SaveMode}
import org.elasticsearch.hadoop.cfg.ConfigurationOptions
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.util.{EsUtils, JdbcOptions, Properties}

import java.net.URI

/**
 * ElasticSearch实现
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
class EsSink(val props: Properties) extends Sink {

  protected val logger: Logger = LoggerFactory.getLogger(classOf[EsSink])

  override def write(df: DataFrame): Unit = {
    val url = props.getString(JdbcOptions.URL)
    val user = props.getString(JdbcOptions.USER)
    val password = props.getString(JdbcOptions.PASSWORD)
    val table = props.getString(JdbcOptions.TABLE)
    val batchSize = props.getInt(JdbcOptions.BATCH_SIZE, 4096)
    logger.info("写入ES索引: {}", table)

    val endpoint = if (url.startsWith(EsUtils.esUrlPrefix)) {
      url.substring(EsUtils.esUrlPrefix.length)
    } else if (url.startsWith(EsUtils.opendistroUrlPrefix)) {
      url.substring(EsUtils.opendistroUrlPrefix.length)
    } else {
      throw new UnsupportedOperationException(s"不支持的ES数据源: $url")
    }
    val uri = URI.create(endpoint)
    val writer = df.write.format("org.elasticsearch.spark.sql")
      .option(ConfigurationOptions.ES_NODES, uri.getHost)
      .option(ConfigurationOptions.ES_PORT, uri.getPort)
      .option(ConfigurationOptions.ES_NET_HTTP_AUTH_USER, user)
      .option(ConfigurationOptions.ES_NET_HTTP_AUTH_PASS, password)
      .option(ConfigurationOptions.ES_HTTP_TIMEOUT, "10s")
      .option(ConfigurationOptions.ES_NODES_WAN_ONLY, value = true)
      // 是否自动创建index
      .option(ConfigurationOptions.ES_INDEX_AUTO_CREATE, value = true)
      .option(ConfigurationOptions.ES_BATCH_SIZE_ENTRIES, batchSize)

    // es会为每个文档分配一个全局id。如果不指定此参数将随机生成；如果指定的话按指定的来
    val id = props.getString("id")
    if (id != null) {
      logger.info("_id映射: {}", id)
      writer.option(ConfigurationOptions.ES_MAPPING_ID, id)
    }
    writer.mode(SaveMode.Append).save(table)
  }

}

class EsSinkFactory extends SinkFactory {

  override def accept(url: String): Boolean =
    url.startsWith(EsUtils.esUrlPrefix) || url.startsWith(EsUtils.opendistroUrlPrefix)

  override def create(props: Properties): Sink = new EsSink(props)

}
