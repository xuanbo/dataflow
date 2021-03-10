package tk.fishfish.dataflow.core

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.{Logger, LoggerFactory}
import tk.fishfish.dataflow.util.{CollectionUtils, StringUtils, Validation}

/**
 * 转换
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Transformer extends Task {

  def transform(df: DataFrame, conf: Conf): DataFrame

}

class DefaultTransformer(val spark: SparkSession) extends Transformer {

  private val logger: Logger = LoggerFactory.getLogger(classOf[IoTSource])

  override def taskType(): String = "DEFAULT_TRANSFORMER"

  override def transform(df: DataFrame, conf: Conf): DataFrame = {
    Validation.notNull(conf.columns, "配置 [conf.columns] 不能为空")
    val seq = conf.columns
      .map { column =>
        if (CollectionUtils.isEmpty( column.transforms)) {
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
            if (StringUtils.isNotEmpty(func)) {
              name = func
            }
            if (StringUtils.isNotEmpty(rename)) {
              name += s" AS $rename"
            }
          }
          name
        }
      }.filter(StringUtils.isNotEmpty)
    if (seq.nonEmpty) {
      logger.info("转换表达式: {}", seq.mkString(", "))
      return df.selectExpr(seq: _*)
    }
    df
  }

}
