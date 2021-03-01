package tk.fishfish.dataflow.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{PostMapping, RequestBody, RequestMapping, RestController}
import tk.fishfish.dataflow.dag.{Dag, DagExecutor, Graph}

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
  def run(@RequestBody graph: Graph): Unit = {
    val dag = Dag(graph)
    dagExecutor.run(dag)
  }

}
