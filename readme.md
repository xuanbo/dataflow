# dataflow

> 基于 Spark 任务流执行平台

## 依赖

- Scala 2.11.12
- Spark 2.4.7
- Spring Boot 2.3.7.RELEASE

## 节点类型

### Source

源端

- SQL_SOURCE

    通用 JDBC 读取
     
    ```json5
    {
        "id":"NODE_ID",
        "name":"节点名称",
        // 节点类型
        "nodeType":"SQL_SOURCE",
        "conf":{
            "jdbc": {
                "driver": "com.mysql.cj.jdbc.Driver",
                "url": "jdbc:mysql://127.0.0.1:3306/dataflow?useUnicode=true&characterEncoding=UTF-8",
                "user": "root",
                "password": "123456",
                // 读取表
                "table": "test"
            },
            // 表字段
            "columns":[
                {
                    "name":"id",
                    // 类型暂未使用
                    "type":"INT"
                },
                {
                    "name":"name",
                    "type":"STRING"
                },
                {
                    "name":"age",
                    "type":"INT"
                }
            ]
        }
    }
    ```

- IOT_SOURCE

    IoTDB 读取
    
    ```json5
    {
        "id":"NODE_ID",
        "name":"节点名称",
        // 节点类型
        "nodeType":"IOT_SOURCE",
        "conf":{
            "jdbc": {
                "url": "jdbc:iotdb://127.0.0.1:6667/",
                "user": "root",
                "password": "123456",
                // 读取表
                "table": "root.test"
            },
            // 表字段
            "columns":[
                {
                    "name":"id",
                    // 类型暂未使用
                    "type":"INT"
                },
                {
                    "name":"name",
                    "type":"STRING"
                },
                {
                    "name":"age",
                    "type":"INT"
                }
            ]
        }
    }
    ```

### Transformer

转换

- DEFAULT_TRANSFORMER

    ```json5
    {
        "id":"NODE_ID",
        "name":"节点名称",
        // 节点类型
        "nodeType":"DEFAULT_TRANSFORMER",
        "conf":{
            "columns":[
                {
                    "name":"id",
                    "type":"INT",
                    // 转换函数，这里不处理
                    "transforms":[
                    ]
                },
                {
                    "name":"name",
                    "type":"STRING",
                    // 转换函数，重命名字段
                    "transforms":[
                        {
                            "type":"RENAME",
                            "value":"NAME"
                        }
                    ]
                },
                {
                    "name":"age",
                    "type":"INT",
                    // 转换函数，执行 SQL 函数（Spark SQL）
                    "transforms": [
                        {
                            "type": "FUNC",
                            "value": "IFNULL(age, 0)"
                        },
                            {
                            "type": "RENAME",
                            "value": "age"
                            }
                    ]
                }
            ]
        }
    }
    ```

### Filter

过滤

- SQL_FILTER

    ```json5
    {
        "id":"NODE_ID",
        "name":"节点名称",
        // 节点类型
        "nodeType":"SQL_FILTER",
        "conf":{
            // 过滤条件（SQL）
            "conditions": [
                "age >= 25"
            ],
            "columns":[
                {
                    "name":"id",
                    "type":"INT"
                },
                {
                    "name":"name",
                    "type":"STRING"
                },
                {
                    "name":"age",
                    "type":"INT"
                }
            ]
        }
    }
    ```

### Target

目标端

- SQL_TARGET

    通用 JDBC 写入
    
    ```json5
    {
        "id":"NODE_ID",
        "name":"节点名称",
        // 节点类型
        "nodeType":"SQL_TARGET",
          "conf": {
            "jdbc": {
              "driver": "com.mysql.cj.jdbc.Driver",
              "url": "jdbc:mysql://127.0.0.1:3306/dataflow?useUnicode=true&characterEncoding=UTF-8",
              "user": "root",
              "password": "123456",
              // 写入表名
              "table": "test1"
            }
          }
    }
    ```

- LOG_TARGET

    输出结果，方便调试
    
    ```json5
    {
        "id":"NODE_ID",
        "name":"节点名称",
        // 节点类型
        "nodeType":"LOG_TARGET"
    }
    ```

## API

待完善

### 提交任务

> 不支持多节点合并

![dag](./docs/dag.png)

```text
POST /v1/dag/run

{
    "nodes":[
        {
            "id":"1",
            "name":"读",
            "nodeType":"SQL_SOURCE",
            "conf":{
                "jdbc": {
                    "driver": "com.mysql.cj.jdbc.Driver",
                    "url": "jdbc:mysql://127.0.0.1:3306/dataflow?useUnicode=true&characterEncoding=UTF-8",
                    "user": "root",
                    "password": "123456",
                    "table": "test"
                },
                "columns":[
                    {
                        "name":"id",
                        "type":"INT"
                    },
                    {
                        "name":"name",
                        "type":"STRING"
                    },
                    {
                        "name":"age",
                        "type":"INT"
                    }
                ]
            }
        },
        {
            "id":"2",
            "name":"转换",
            "nodeType":"DEFAULT_TRANSFORMER",
            "conf":{
                "columns":[
                    {
                        "name":"id",
                        "type":"INT",
                        "transforms":[
                        ]
                    },
                    {
                        "name":"name",
                        "type":"STRING",
                        "transforms":[
                            {
                                "type":"RENAME",
                                "value":"name1"
                            }
                        ]
                    },
                    {
                        "name":"age",
                        "type":"INT",
                        "transforms":[
                            {
                                "type":"RENAME",
                                "value":"age1"
                            }
                        ]
                    }
                ]
            }
        },
        {
            "id":"6",
            "name":"读",
            "nodeType":"SQL_SOURCE",
            "conf":{
                "jdbc": {
                    "driver": "com.mysql.cj.jdbc.Driver",
                    "url": "jdbc:mysql://127.0.0.1:3306/dataflow?useUnicode=true&characterEncoding=UTF-8",
                    "user": "root",
                    "password": "123456",
                    "table": "test"
                },
                "columns":[
                    {
                        "name":"id",
                        "type":"INT"
                    },
                    {
                        "name":"name",
                        "type":"STRING"
                    },
                    {
                        "name":"age",
                        "type":"INT"
                    }
                ]
            }
        },
        {
            "id":"3",
            "name":"过滤",
            "nodeType":"SQL_FILTER",
            "conf":{
                "conditions":[
                    "age1 >= 25"
                ],
                "columns":[
                    {
                        "name":"id",
                        "type":"INT"
                    },
                    {
                        "name":"name1",
                        "type":"STRING"
                    },
                    {
                        "name":"age1",
                        "type":"INT"
                    }
                ]
            }
        },
        {
            "id":"4",
            "name":"写",
            "nodeType":"SQL_TARGET",
            "conf":{
                "jdbc": {
                    "driver": "com.mysql.cj.jdbc.Driver",
                    "url": "jdbc:mysql://127.0.0.1:3306/dataflow?useUnicode=true&characterEncoding=UTF-8",
                    "user": "root",
                    "password": "123456",
                    "table": "test1"
                },
                "columns":[
                    {
                        "name":"id",
                        "type":"INT"
                    },
                    {
                        "name":"name1",
                        "type":"STRING"
                    },
                    {
                        "name":"age1",
                        "type":"INT"
                    }
                ]
            }
        },
        {
            "id":"5",
            "name":"写",
            "nodeType":"LOG_TARGET",
            "conf":{}
        },
        {
            "id":"7",
            "name":"写",
            "nodeType":"LOG_TARGET",
            "conf":{}
        }
    ],
    "edges":[
        {
            "from":"1",
            "to":"2"
        },
        {
            "from":"2",
            "to":"5"
        },
        {
            "from":"2",
            "to":"3"
        },
        {
            "from":"3",
            "to":"4"
        },
        {
            "from":"6",
            "to":"7"
        }
    ]
}
```

如上的配置拆分 3 个任务提交到 Spark 运行：

- 1 -> 2 -> 5
- 1 -> 2 -> 3 -> 4
- 6 -> 7

并会在运行时缓存节点：

- 1
- 2

暂未做到更智能的只缓存节点：2
