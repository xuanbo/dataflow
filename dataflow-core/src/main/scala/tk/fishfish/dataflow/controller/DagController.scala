package tk.fishfish.dataflow.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{PostMapping, RequestBody, RequestMapping, RequestParam, RestController}
import tk.fishfish.dataflow.dag.{DagExecutor, ExecutionParam}

import scala.collection.mutable

/**
 * DAG接口
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@RestController
@RequestMapping(Array("/v1/dag"))
class DagController {

  @Autowired
  private val dagExecutor: DagExecutor = null

  @PostMapping(Array("/run"))
  def run(@RequestBody param: ExecutionParam): Unit = {
    val executionParam = if (param.context == null) {
      ExecutionParam(param.executionId, param.graphId, mutable.HashMap[String, Any](), param.graph, param.callback)
    } else {
      param
    }
    dagExecutor.run(executionParam)
  }

  @PostMapping(Array("/cancel"))
  def cancel(@RequestParam executionId: String): Unit = dagExecutor.cancel(executionId)

}
