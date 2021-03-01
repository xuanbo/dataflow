# dataflow

> 基于 Spark 任务流

## 文档

待完善

## API

待完善

### 提交任务

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
                    "url": "jdbc:mysql://egova.top:30010/test_cdb?useUnicode=true&characterEncoding=UTF-8",
                    "user": "root",
                    "password": "egova",
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
                    "url": "jdbc:mysql://egova.top:30010/test_cdb?useUnicode=true&characterEncoding=UTF-8",
                    "user": "root",
                    "password": "egova",
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
            "nodeType":"LOG_TARGET",
            "conf":{
                "jdbc": {
                    "driver": "com.mysql.cj.jdbc.Driver",
                    "url": "jdbc:mysql://egova.top:30010/test_cdb?useUnicode=true&characterEncoding=UTF-8",
                    "user": "root",
                    "password": "egova",
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
            "conf":{
                "jdbc": {
                    "driver": "com.mysql.cj.jdbc.Driver",
                    "url": "jdbc:mysql://egova.top:30010/test_cdb?useUnicode=true&characterEncoding=UTF-8",
                    "user": "root",
                    "password": "egova",
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
            "id":"7",
            "name":"写",
            "nodeType":"LOG_TARGET",
            "conf":{
                "jdbc": {
                    "driver": "com.mysql.cj.jdbc.Driver",
                    "url": "jdbc:mysql://egova.top:30010/test_cdb?useUnicode=true&characterEncoding=UTF-8",
                    "user": "root",
                    "password": "egova",
                    "table": "test1"
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

如上的配置拆封成 Spark 任务提交：

- 1 -> 2 -> 5
- 1 -> 2 -> 3 -> 4
- 6 -> 7
