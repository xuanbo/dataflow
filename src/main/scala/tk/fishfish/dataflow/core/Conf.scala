package tk.fishfish.dataflow.core

import com.esotericsoftware.kryo.NotNull
import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty}
import org.springframework.validation.annotation.Validated

import javax.validation.constraints.NotBlank

/**
 * 配置
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
case class Conf(@NotBlank columns: Seq[Column], conditions: Seq[String], jdbc: Jdbc, kafka: Kafka)

@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
case class Column(@NotBlank name: String, @NotBlank @JsonProperty("type") columnType: String, transforms: Seq[Transform])

@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
case class Transform(@NotNull @JsonProperty("type") transformType: String, @NotBlank value: String)

@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
case class Jdbc(@NotBlank driver: String, @NotBlank url: String, @NotBlank user: String, @NotBlank password: String, @NotBlank table: String)

@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
case class Kafka(@NotBlank brokers: String, @NotBlank topic: String, @NotBlank table: String)
