package tk.fishfish.dataflow.core

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * 过滤
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
trait Filter extends Task {

  def supportNext(): Seq[Class[_]] = Seq(classOf[Transformer], classOf[Filter], classOf[Target])

  def filter(df: DataFrame, conf: Conf): DataFrame

}

class SqlFilter(val spark: SparkSession) extends Filter {

  override def taskType(): String = "SQL_FILTER"

  override def filter(df: DataFrame, conf: Conf): DataFrame = {
    if (df == null) {
      return df
    }
    if (conf.conditions == null || conf.conditions.isEmpty) {
      return df
    }
    var res: DataFrame = df
    conf.conditions.filter(e => StringUtils.isNotBlank(e)).foreach(e => res = res.filter(e))
    res
  }

}
