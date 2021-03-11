package tk.fishfish.dataflow.dag

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import tk.fishfish.dataflow.Bootstrap

/**
 * DAG测试
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[Bootstrap]))
class DagTest {

  private val logger: Logger = LoggerFactory.getLogger(classOf[DagTest])

  @Autowired
  private val objectMapper: ObjectMapper = null

  @Autowired
  private val dagExecutor: DagExecutor = null

  private val json =
    """
      |{
      |    "nodes":[
      |        {
      |            "id":"1",
      |            "name":"读",
      |            "nodeType":"SQL_SOURCE",
      |            "conf":{
      |                "jdbc": {
      |                    "driver": "com.mysql.cj.jdbc.Driver",
      |                    "url": "jdbc:mysql://127.0.0.1:3306/dataflow?useUnicode=true&characterEncoding=UTF-8",
      |                    "user": "root",
      |                    "password": "123456",
      |                    "table": "test"
      |                },
      |                "columns":[
      |                    {
      |                        "name":"id",
      |                        "type":"INT"
      |                    },
      |                    {
      |                        "name":"name",
      |                        "type":"STRING"
      |                    },
      |                    {
      |                        "name":"age",
      |                        "type":"INT"
      |                    }
      |                ]
      |            }
      |        },
      |        {
      |            "id":"2",
      |            "name":"转换",
      |            "nodeType":"DEFAULT_TRANSFORMER",
      |            "conf":{
      |                "columns":[
      |                    {
      |                        "name":"id",
      |                        "type":"INT",
      |                        "transforms":[
      |                        ]
      |                    },
      |                    {
      |                        "name":"name",
      |                        "type":"STRING",
      |                        "transforms":[
      |                            {
      |                                "type":"RENAME",
      |                                "value":"name1"
      |                            }
      |                        ]
      |                    },
      |                    {
      |                        "name":"age",
      |                        "type":"INT",
      |                        "transforms":[
      |                            {
      |                                "type":"RENAME",
      |                                "value":"age1"
      |                            }
      |                        ]
      |                    }
      |                ]
      |            }
      |        },
      |        {
      |            "id":"6",
      |            "name":"读",
      |            "nodeType":"SQL_SOURCE",
      |            "conf":{
      |                "jdbc": {
      |                    "driver": "com.mysql.cj.jdbc.Driver",
      |                    "url": "jdbc:mysql://127.0.0.1:3306/dataflow?useUnicode=true&characterEncoding=UTF-8",
      |                    "user": "root",
      |                    "password": "123456",
      |                    "table": "test"
      |                },
      |                "columns":[
      |                    {
      |                        "name":"id",
      |                        "type":"INT"
      |                    },
      |                    {
      |                        "name":"name",
      |                        "type":"STRING"
      |                    },
      |                    {
      |                        "name":"age",
      |                        "type":"INT"
      |                    }
      |                ]
      |            }
      |        },
      |        {
      |            "id":"3",
      |            "name":"过滤",
      |            "nodeType":"SQL_FILTER",
      |            "conf":{
      |                "conditions":[
      |                    "age1 >= 25"
      |                ],
      |                "columns":[
      |                    {
      |                        "name":"id",
      |                        "type":"INT"
      |                    },
      |                    {
      |                        "name":"name1",
      |                        "type":"STRING"
      |                    },
      |                    {
      |                        "name":"age1",
      |                        "type":"INT"
      |                    }
      |                ]
      |            }
      |        },
      |        {
      |            "id":"4",
      |            "name":"写",
      |            "nodeType":"SQL_TARGET",
      |            "conf":{
      |                "jdbc": {
      |                    "driver": "com.mysql.cj.jdbc.Driver",
      |                    "url": "jdbc:mysql://127.0.0.1:3306/dataflow?useUnicode=true&characterEncoding=UTF-8",
      |                    "user": "root",
      |                    "password": "123456",
      |                    "table": "test1"
      |                },
      |                "columns":[
      |                    {
      |                        "name":"id",
      |                        "type":"INT"
      |                    },
      |                    {
      |                        "name":"name1",
      |                        "type":"STRING"
      |                    },
      |                    {
      |                        "name":"age1",
      |                        "type":"INT"
      |                    }
      |                ]
      |            }
      |        },
      |        {
      |            "id":"5",
      |            "name":"写",
      |            "nodeType":"LOG_TARGET",
      |            "conf":{}
      |        },
      |        {
      |            "id":"7",
      |            "name":"写",
      |            "nodeType":"LOG_TARGET",
      |            "conf":{}
      |        }
      |    ],
      |    "edges":[
      |        {
      |            "from":"1",
      |            "to":"2"
      |        },
      |        {
      |            "from":"2",
      |            "to":"5"
      |        },
      |        {
      |            "from":"2",
      |            "to":"3"
      |        },
      |        {
      |            "from":"3",
      |            "to":"4"
      |        },
      |        {
      |            "from":"6",
      |            "to":"7"
      |        }
      |    ]
      |}
      |""".stripMargin

  @Test
  def run(): Unit = {
    val graph = objectMapper.readValue(json, classOf[Graph])
    logger.info("graph: {}", graph)
    dagExecutor.run("test", graph)
  }

  @Test
  def simplePath(): Unit = {
    val graph = objectMapper.readValue(json, classOf[Graph])
    logger.info("graph: {}", graph)
    val paths = Dag.simplePaths(graph)
    for (path <- paths) {
      logger.info("flow: {}", path.mkString(" -> "))
    }
  }

}
