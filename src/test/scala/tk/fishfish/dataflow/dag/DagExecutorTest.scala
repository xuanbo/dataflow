package tk.fishfish.dataflow.dag

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import tk.fishfish.dataflow.Bootstrap

/**
 * Dag测试
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[Bootstrap]))
class DagExecutorTest {

  @Autowired
  private val objectMapper: ObjectMapper = null

  @Autowired
  private val dagExecutor: DagExecutor = null

  @Test
  def run(): Unit = {
    val graph = objectMapper.readValue(
      """
        |{
        |    "nodes":[
        |        {
        |            "id":"1",
        |            "name":"SOURCE_SQL",
        |            "text":"读",
        |            "argument":{
        |                "input":{
        |                    "url":"jdbc:mysql://localhost:3306/dataflow?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8",
        |                    "user":"root",
        |                    "password":"123456",
        |                    "sql":"select * from df_execution"
        |                },
        |                "output":{
        |                    "table":"df_execution"
        |                }
        |            }
        |        },
        |        {
        |            "id":"2",
        |            "name":"TARGET_LOG",
        |            "text":"写",
        |            "argument":{
        |                "input":{
        |                    "table":"df_execution"
        |                },
        |                "output":{
        |                    "table":"df_execution"
        |                }
        |            }
        |        }
        |    ],
        |    "edges":[
        |        {
        |            "from":"1",
        |            "to":"2"
        |        }
        |    ]
        |}
        |""".stripMargin, classOf[Graph])
    dagExecutor.run("test", Dag(graph))
  }

}
