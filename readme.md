# dataflow

> 基于 Spark 任务流执行平台

## 依赖

- Scala 2.11.12
- Spark 2.4.7
- Spring Boot 2.3.7.RELEASE

## 模块介绍

```text
├── dataflow-core				核心模块
├── dataflow-launch			启动		
├── dataflow-plugin			插件（组件）
```

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

- [组件介绍文档](./component.md)
- [插件开发指北](./plugin.md)

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
