# 组件介绍

> 组件介绍

## 源端

### SQL源端

从源端抽取读取，目前支持 MySQL、ES、达梦、Oracle、SqlServer、PostgreSQL

组件定义：

```json5
{
  "group": "BASIC",
  "name": "DATABASE_SOURCE",
  "text": "数据库源端",
  "argument": {
    "input": [
      {
        "name": "url",
        "text": "JDBC 连接",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "JDBC 连接\r\njdbc:mysql://127.0.0.1:3306/test\r\njdbc:es://http://127.0.0.1:9200\r\njdbc:dm://127.0.0.1:5236\r\njdbc:sqlserver://127.0.0.1:1433;DatabaseName=testdb\r\njdbc:oracle:thin:@127.0.0.1:1521:testdb\r\njdbc:postgresql://127.0.0.1:5432/postgres"
      },
      {
        "name": "user",
        "text": "JDBC 用户名",
        "type": "string",
        "component": "input",
        "required": false,
        "remark": "JDBC 用户名"
      },
      {
        "name": "password",
        "text": "JDBC 密码",
        "type": "string",
        "component": "password",
        "required": false,
        "remark": "JDBC 密码"
      },
      {
        "name": "sql",
        "text": "SQL 语句",
        "type": "string",
        "component": "textarea",
        "required": true,
        "remark": "源端读取 SQL 语句"
      }
    ],
    "output": [
      {
        "name": "table",
        "text": "表",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "结果表"
      }
    ]
  }
}
```

示例数据：

```json5
{
  "id": "1",
  "name": "DATABASE_SOURCE",
  "text": "读",
  "argument": {
    "input": {
      "url": "jdbc:mysql://127.0.0.1:3306/test?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8",
      "user": "root",
      "password": "123456",
      "sql": "select * from `bobo_test-bk`"
    },
    "output": {
      "table": "t1"
    }
  }
}
```

### 文件输入源端

读取给定格式的文件，支持 csv、json、parquet、orc 格式

组件定义：

```json5
{
  "group": "BASIC",
  "name": "FILE_SOURCE",
  "text": "文件输入源端",
  "argument": {
    "input": [
      {
        "name": "path",
        "text": "输入的文件目录",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "/some/path/dir"
      },
      {
        "name": "format",
        "text": "文件格式",
        "type": "string",
        "component": "single-select",
        "options": [
          {
            "label": "csv",
            "value": "csv"
          },
          {
            "label": "json",
            "value": "json"
          },
          {
            "label": "parquet",
            "value": "parquet"
          },
          {
            "label": "orc",
            "value": "orc"
          }
        ],
        "required": true,
        "remark": "输入的文件格式类型"
      }
    ],
    "output": [
      {
        "name": "table",
        "text": "表",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "结果表"
      }
    ]
  }
}
```

示例数据：

```json5
{
  "id": "1",
  "name": "FILE_SOURCE",
  "text": "读",
  "argument": {
    "input": {
      "path": "/Users/xuanbo/Downloads/spark-warehouse/n_1_t1",
      "format": "parquet"
    },
    "output": {
      "table": "t1"
    }
  }
}
```

## 转换

### SQL转换

数据转换，支持 SQL 查询语法（底层基于 Spark SQL 语法实现）

组件定义：

```json5
{
  "group": "BASIC",
  "name": "SQL_TRANSFORMER",
  "text": "SQL转换",
  "argument": {
    "input": [
      {
        "name": "sql",
        "text": "查询SQL语句",
        "type": "string",
        "component": "textarea",
        "required": true,
        "remark": "SQL查询语句"
      }
    ],
    "output": [
      {
        "name": "table",
        "text": "表",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "结果表"
      }
    ]
  }
}
```

示例数据：

```json5
{
  "id": "2",
  "name": "SQL_TRANSFORMER",
  "text": "转换",
  "argument": {
    "input": {
      "sql": "select * from {namespace}.t1 where name is not null"
    },
    "output": {
      "table": "t2"
    }
  }
}
```

## 脚本

### SHELL任务

执行shell任务，例如运行 python 脚本

组件定义：

```json5
{
  "group": "BASIC",
  "name": "SHELL",
  "text": "执行shell命令",
  "argument": {
    "input": [
      {
        "name": "command",
        "text": "命令",
        "type": "string",
        "component": "textarea",
        "required": true,
        "remark": "需要执行的shell命令，比如：python /some/path/xxx.py"
      }
    ],
    "output": []
  }
}
```

示例数据：

```json5
{
  "id": "4",
  "name": "SHELL",
  "text": "shell",
  "argument": {
    "input": {
      "command": "python /some/path/xxx.py"
    },
    "output": {
    }
  }
}
```

### SQL脚本执行

在数据库中运行 SQL 语法，比如建表、新增字段等。目前支持 MySQL、达梦、Oracle、SqlServer、PostgreSQL

组件定义：

```json5
{
  "group": "BASIC",
  "name": "SQL_EXECUTOR",
  "text": "SQL脚本执行",
  "argument": {
    "input": [
      {
        "name": "url",
        "text": "JDBC 连接",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "JDBC 连接\r\njdbc:mysql://127.0.0.1:3306/test\r\njdbc:es://http://127.0.0.1:9200\r\njdbc:dm://127.0.0.1:5236\r\njdbc:sqlserver://127.0.0.1:1433;DatabaseName=testdb\r\njdbc:oracle:thin:@127.0.0.1:1521:testdb\r\njdbc:postgresql://127.0.0.1:5432/postgres"
      },
      {
        "name": "user",
        "text": "JDBC 用户名",
        "type": "string",
        "component": "input",
        "required": false,
        "remark": "JDBC 用户名"
      },
      {
        "name": "password",
        "text": "JDBC 密码",
        "type": "string",
        "component": "password",
        "required": false,
        "remark": "JDBC 密码"
      },
      {
        "name": "sql",
        "text": "SQL 执行语句",
        "type": "string",
        "component": "textarea",
        "required": true,
        "remark": "SQL 执行语句，可执行多条 SQL 执行（用分号隔开）"
      }
    ],
    "output": [
    ]
  }
}
```

示例数据：

```json5
{
  "id": "4",
  "name": "SQL_EXECUTOR",
  "text": "SQL脚本执行",
  "argument": {
    "input": {
      "url": "jdbc:mysql://127.0.0.1:3306/test?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8",
      "user": "root",
      "password": "123456",
      "sql": "show tables;show databases"
    },
    "output": {
    }
  }
}
```

## 目标端

### SQL目标端

数据写入目标端，目前支持 MySQL、ES、达梦、Oracle、SqlServer、PostgreSQL

组件定义：

```json5
{
  "group": "BASIC",
  "name": "DATABASE_SINK",
  "text": "数据库目标端",
  "argument": {
    "input": [
      {
        "name": "table",
        "text": "表",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "读取表"
      }
    ],
    "output": [
      {
        "name": "url",
        "text": "JDBC 连接",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "JDBC 连接\r\njdbc:mysql://127.0.0.1:3306/test\r\njdbc:es://http://127.0.0.1:9200\r\njdbc:dm://127.0.0.1:5236\r\njdbc:sqlserver://127.0.0.1:1433;DatabaseName=testdb\r\njdbc:oracle:thin:@127.0.0.1:1521:testdb\r\njdbc:postgresql://127.0.0.1:5432/postgres"
      },
      {
        "name": "user",
        "text": "JDBC 用户名",
        "type": "string",
        "component": "input",
        "required": false,
        "remark": "JDBC 用户名"
      },
      {
        "name": "password",
        "text": "JDBC 密码",
        "type": "string",
        "component": "password",
        "required": false,
        "remark": "JDBC 密码"
      },
      {
        "name": "table",
        "text": "表名",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "目标端写入数据库表名"
      }
    ]
  }
}
```

示例数据：

```json5
{
  "id": "3",
  "name": "DATABASE_SINK",
  "text": "写",
  "argument": {
    "input": {
      "table": "t2"
    },
    "output": {
      "url": "jdbc:es://http://127.0.0.1:9200",
      "user": "elastic",
      "password": "123456",
      "table": "bobo_test",
      "id": "id"
    }
  }
}
```

### 文件输出目标端

输出给定格式的文件，支持 csv、json、parquet、orc 格式

组件定义：

```json5
{
  "group": "BASIC",
  "name": "FILE_SINK",
  "text": "文件输出目标端",
  "argument": {
    "input": [
      {
        "name": "table",
        "text": "表",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "读取表"
      }
    ],
    "output": [
      {
        "name": "path",
        "text": "输出的文件目录",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "/some/path/dir"
      },
      {
        "name": "format",
        "text": "文件格式",
        "type": "string",
        "component": "single-select",
        "options": [
          {
            "label": "csv",
            "value": "csv"
          },
          {
            "label": "json",
            "value": "json"
          },
          {
            "label": "parquet",
            "value": "parquet"
          },
          {
            "label": "orc",
            "value": "orc"
          }
        ],
        "required": true,
        "remark": "输出的文件格式类型"
      }
    ]
  }
}
```

示例数据：

```json5
{
  "id": "3",
  "name": "FILE_SINK",
  "text": "写",
  "argument": {
    "input": {
      "table": "t2"
    },
    "output": {
      "path": "/some/path/dir",
      "format": "parquet"
    }
  }
}
```
