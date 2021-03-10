package tk.fishfish.dataflow.core

import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty}

/**
 * 配置
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
case class Conf(columns: Seq[Column], conditions: Seq[String], jdbc: Jdbc)

@JsonInclude(JsonInclude.Include.NON_NULL)
case class Column(name: String, @JsonProperty("type") columnType: String, transforms: Seq[Transform])

@JsonInclude(JsonInclude.Include.NON_NULL)
case class Transform(@JsonProperty("type") transformType: String, value: String)

@JsonInclude(JsonInclude.Include.NON_NULL)
case class Jdbc(driver: String, url: String, user: String, password: String, table: String, sql: String)
