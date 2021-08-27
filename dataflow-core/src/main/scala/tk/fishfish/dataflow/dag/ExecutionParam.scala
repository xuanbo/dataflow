package tk.fishfish.dataflow.dag

import javax.validation.constraints.{NotBlank, NotNull}
import scala.collection.mutable

/**
 * 执行参数
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
case class ExecutionParam(@NotBlank executionId: String,
                          graphId: String,
                          context: mutable.Map[String, Any],
                          @NotNull graph: Graph,
                          var callback: String)
