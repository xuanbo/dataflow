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

  @Test
  def sqlSource2sqlTarget(): Unit = {
    val graph = objectMapper.readValue(
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
        |                    "url": "jdbc:mysql://egova.top:30010/test_cdb?useUnicode=true&characterEncoding=UTF-8",
        |                    "user": "root",
        |                    "password": "egova",
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
        |                            },
        |                            {
        |                                "type":"FUNC",
        |                                "value":"DICT(name, '_', '123')"
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
        |                    "url": "jdbc:mysql://egova.top:30010/test_cdb?useUnicode=true&characterEncoding=UTF-8",
        |                    "user": "root",
        |                    "password": "egova",
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
        |        }
        |    ],
        |    "edges":[
        |        {
        |            "from":"1",
        |            "to":"2"
        |        },
        |        {
        |            "from":"2",
        |            "to":"3"
        |        },
        |        {
        |            "from":"3",
        |            "to":"4"
        |        }
        |    ]
        |}
        |""".stripMargin, classOf[Graph])
    logger.info("graph: {}", graph)
    val dag = Dag(graph)
    val str = objectMapper.writeValueAsString(dag)
    logger.info("dag: {}", str)

    dagExecutor.run(dag)
  }

  @Test
  def sqlSource2kafkaTarget(): Unit = {
    val graph = objectMapper.readValue(
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
        |                    "url": "jdbc:mysql://egova.top:30010/test_cdb?useUnicode=true&characterEncoding=UTF-8",
        |                    "user": "root",
        |                    "password": "egova",
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
        |            "name":"写",
        |            "nodeType":"KAFKA_TARGET",
        |            "conf":{
        |                "kafka": {
        |                    "brokers": "127.0.0.1:9092",
        |                    "topic": "test"
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
    logger.info("graph: {}", graph)
    val dag = Dag(graph)
    val str = objectMapper.writeValueAsString(dag)
    logger.info("dag: {}", str)

    dagExecutor.run(dag)
  }

}
