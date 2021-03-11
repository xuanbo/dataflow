package tk.fishfish.dataflow.core

import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty}

/**
 * 配置
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class Conf {

  var columns: Seq[Column] = _

  var conditions: Seq[String] = _

  var jdbc: Jdbc = _

}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Column {

  var name: String = _

  @JsonProperty("type")
  var columnType: String = _

  var transforms: Seq[Transform] = _

}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Transform {

  @JsonProperty("type")
  var transformType: String = _

  var value: String = _

}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Jdbc {

  /**
   * 数据源ID
   */
  var id: String = _

  var url: String = _

  var user: String = _

  var password: String = _

  /**
   * 表
   */
  var table: String = _

  /**
   * 自定义SQL，优先级高于[[table]]
   */
  var sql: String = _

}
