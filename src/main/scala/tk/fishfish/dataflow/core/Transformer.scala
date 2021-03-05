package tk.fishfish.dataflow.core

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.exception.FlowException

/**
 * 转换
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Transformer extends Task {

  def supportNext(): Seq[Class[_]] = Seq(classOf[Transformer], classOf[Filter], classOf[Target])

  def transform(df: DataFrame, conf: Conf): DataFrame

}

class DefaultTransformer(val spark: SparkSession) extends Transformer {

  private val logger: Logger = LoggerFactory.getLogger(classOf[IoTSource])

  override def taskType(): String = "DEFAULT_TRANSFORMER"

  override def transform(df: DataFrame, conf: Conf): DataFrame = {
    if (conf.columns == null) {
      throw new FlowException("配置[conf.columns]不能为空")
    }
    val seq = conf.columns
      .map { column =>
        if (column.transforms == null || column.transforms.isEmpty) {
          column.name
        } else {
          var name: String = column.name
          var rename: String = ""
          var func: String = ""
          var drop: Boolean = false
          column.transforms.foreach { transform =>
            transform.transformType match {
              case "RENAME" => rename = transform.value
              case "FUNC" => func = transform.value
              case "DROP" => drop = true
            }
          }
          if (drop) {
            name = ""
          } else {
            if (StringUtils.isNotBlank(func)) {
              name = func
            }
            if (StringUtils.isNotBlank(rename)) {
              name += s" AS $rename"
            }
          }
          name
        }
      }.filter(e => StringUtils.isNotBlank(e))
    if (seq.nonEmpty) {
      logger.info("转换表达式: {}", seq.mkString(", "))
      return df.selectExpr(seq: _*)
    }
    df
  }

}
