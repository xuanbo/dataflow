package tk.fishfish.dataflow.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession}
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.exception.FlowException

import java.util.Properties
import scala.collection.mutable

/**
 * 目标
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Target extends Task {

  def supportNext(): Seq[Class[_]] = Seq.empty

  def write(df: DataFrame, conf: Conf): Unit

}

class LogTarget(val spark: SparkSession) extends Target {

  override def taskType(): String = "LOG_TARGET"

  override def write(df: DataFrame, conf: Conf): Unit = {
    df.show()
  }

}

class SqlTarget(val spark: SparkSession) extends Target {

  override def taskType(): String = "SQL_TARGET"

  override def write(df: DataFrame, conf: Conf): Unit = {
    if (conf.jdbc == null) {
      throw new FlowException("配置[conf.jdbc]不能为空")
    }
    if (conf.columns == null) {
      throw new FlowException("配置[conf.columns]不能为空")
    }
    df.write.format("jdbc")
      .option("driver", conf.jdbc.driver)
      .option(JDBCOptions.JDBC_URL, conf.jdbc.url)
      .option("user", conf.jdbc.user)
      .option("password", conf.jdbc.password)
      .option(JDBCOptions.JDBC_TABLE_NAME, conf.jdbc.table)
      .mode(SaveMode.Append)
      .save()
  }

}

class KafkaTarget(val spark: SparkSession) extends Target {

  private val logger: Logger = LoggerFactory.getLogger(classOf[KafkaTarget])

  override def taskType(): String = "KAFKA_TARGET"

  override def write(df: DataFrame, conf: Conf): Unit = {
    if (conf.kafka == null) {
      throw new FlowException("配置[conf.kafka]不能为空")
    }
    if (conf.columns == null) {
      throw new FlowException("配置[conf.columns]不能为空")
    }
    val props = new Properties
    props.put("bootstrap.servers", conf.kafka.brokers)
    df.foreachPartition { rows: Iterator[Row] =>
      logger.debug("写入每个分区的数据到kafka: {}", conf.kafka.topic)
      val producer = new KafkaProducer[String, String](props, new StringSerializer, new StringSerializer)
      val objectMapper = new ObjectMapper
      objectMapper.registerModule(DefaultScalaModule)
      var columns: Seq[Column] = null
      rows.foreach { row =>
        if (columns == null) {
          columns = conf.columns
        }
        val map = mutable.Map[String, Any]()
        columns.foreach { column =>
          map += (column.name -> row.getAs(column.name))
        }
        val value = objectMapper.writeValueAsString(Value(conf.kafka.table, columns, Seq(map)))
        logger.info("value: {}", value)
        val msg = new ProducerRecord[String, String](conf.kafka.topic, value)
        producer.send(msg)
      }
    }
  }

  case class Value(table: String, columns: Seq[Column], data: Seq[Any])

}

class DataXTarget(val spark: SparkSession) extends Target {

  override def taskType(): String = "DATAX_TARGET"

  override def write(df: DataFrame, conf: Conf): Unit = {

  }

}
