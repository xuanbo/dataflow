# dataflow

> 基于 Spark 任务流执行平台

## 依赖

- Scala 2.11.12
- Spark 2.4.7
- Spring Boot 2.3.7.RELEASE

## 介绍

基于 Spark 任务流执行平台，每个节点作为一个任务节点。

## 快速开始

- 修改 src/main/resources/application.yaml 配置文件

  ```yaml
  spring:
    datasource:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/dataflow?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8
      username: root
      password: 123456
  ```

- 启动 src/main/resources/tk.fishfish.dataflow.Bootstrap 程序

## 组件

组件定义如下：

```json5
{
  "id": "组件ID",
  "name": "组件类型",
  "text": "描述",
  "argument": {
    // 输入
    "input": {
    },
    // 输出
    "output": {
    }
  }
}
```

### 基础SQL组件

### SQL源端

> SOURCE_SQL

- 输入

  - url

    JDBC 连接，必填。

    - jdbc:mysql://127.0.0.1:3306/test

    - jdbc:iotdb://127.0.0.1:6667/

  - user

    JDBC 用户名，非必填。

  - password

    JDBC 密码，非必填。

  - sql

    源端读取 SQL 语句，必填。

- 输出

  - table

    结果物化视图，必填。

例子：

```json
{
  "id": "组件ID",
  "name": "SOURCE_SQL",
  "text": "描述",
  "argument": {
    "input": {
      "url": "jdbc:mysql://127.0.0.1:3306/test",
      "user": "root",
      "password": "123456",
      "sql": "select * from test"
    },
    "output": {
      "table": "test"
    }
  }
}
```

### 转换

> TRANSFORMER_SQL

- 输入

  - select

    SQL 查询列（支持别名、函数），必填。

  - table

    读取物化视图，必填。

  - groupBy

    SQL GROUP BY 分组语句，非必填。

- 输出

  - table

    结果物化视图，必填。

例子：

```json
{
  "id": "组件ID",
  "name": "TRANSFORMER_SQL",
  "text": "描述",
  "argument": {
    "input": {
      "select": "Time AS time, `root.demo.temperature` AS temperature, `root.demo.hardware` AS hardware",
      "table": "test1"
    },
    "output": {
      "table": "test2"
    }
  }
}
```

### 过滤

> FILTER_SQL

- 输入

  - table

    读取物化视图，必填。

  - where

    SQL WHERE 过滤语句，非必填。

  - orderBy

    SQL ORDER BY 排序语句，非必填。

  - limit

    SQL LIMIT 限制条数语句，非必填。

- 输出

  - table

    结果物化视图，必填。

例子：

```json
{
  "id": "组件ID",
  "name": "FILTER_SQL",
  "text": "描述",
  "argument": {
    "input": {
      "table": "test1",
      "where": "hardware = 'cpu0'",
      "orderBy": "time DESC",
      "limit": 10
    },
    "output": {
      "table": "test2"
    }
  }
}
```

### SQL目标端

> TARGET_SQL

- 输入

  - table

    读取物化视图，必填。

- 输出

  - url

    JDBC 连接，必填。

    - jdbc:mysql://127.0.0.1:3306/test

    - jdbc:iotdb://127.0.0.1:6667/

  - user

    JDBC 用户名，非必填。

  - password

    JDBC 密码，非必填。

  - table

    目标端写入数据库表名，必填。

例子：

```json
{
  "id": "组件ID",
  "name": "TARGET_SQL",
  "text": "描述",
  "argument": {
    "input": {
      "table": "test"
    },
    "output": {
      "url": "jdbc:mysql://127.0.0.1:3306/test",
      "user": "root",
      "password": "123456",
      "table": "test"
    }
  }
}
```

### 合并

> TRANSFORMER_SQL_JOIN

- 输入

  - select

    SQL 查询列（支持别名、函数），必填。

  - table

    读取物化视图，必填。

  - tableAlias

    读取物化视图别名，必填。

  - joins

    表 JOIN 配置，必填。数组。

    - table

      关联物化视图，必填。

    - tableAlias

      关联物化视图别名，必填。

    - on

      关联条件，必填。

  - where

    SQL WHERE 过滤语句，非必填。

  - groupBy

    SQL GROUP BY 分组语句，非必填。

  - orderBy

    SQL ORDER BY 排序语句，非必填。

  - limit

    SQL LIMIT 限制条数语句，非必填。

- 输出

  - table

    结果物化视图，必填。

例子：

```json
{
  "id": "组件ID",
  "name": "TRANSFORMER_SQL_JOIN",
  "text": "描述",
  "argument": {
    "input": {
      "select": "Time AS time, `root.demo.temperature` AS temperature, `root.demo.hardware` AS hardware",
      "table": "test1",
      "tableAlias": "t1",
      "joins": [
        {
          "table": "test2",
          "tableAlias": "t2",
          "on": "t1.id = t2.t_id"
        }
      ]
    },
    "output": {
      "table": "test2"
    }
  }
}
```

### 算法组件

待完善。

## DAG

将任务绘制成流程图，利用 DAG 算法进行节点运算。

```text
POST http://127.0.0.1:9090/v1/dag/run
```

```json
{
  "nodes": [
    {
      "id": "1",
      "name": "SOURCE_SQL",
      "text": "读",
      "argument": {
        "input": {
          "url": "jdbc:iotdb://127.0.0.1:6667/",
          "user": "root",
          "password": "root",
          "sql": "select * from root.demo"
        },
        "output": {
          "table": "demo"
        }
      }
    },
    {
      "id": "2",
      "name": "TRANSFORMER_SQL",
      "text": "转换",
      "argument": {
        "input": {
          "select": "Time AS time, `root.demo.temperature` AS temperature, `root.demo.hardware` AS hardware",
          "table": "demo"
        },
        "output": {
          "table": "demo1"
        }
      }
    },
    {
      "id": "3",
      "name": "TARGET_SQL",
      "text": "写",
      "argument": {
        "input": {
          "table": "demo1"
        },
        "output": {
          "url": "jdbc:mysql://127.0.0.1:3306/dataflow",
          "user": "root",
          "password": "123456",
          "table": "demo"
        }
      }
    }
  ],
  "edges": [
    {
      "from": "1",
      "to": "2"
    },
    {
      "from": "2",
      "to": "3"
    }
  ]
}
```
