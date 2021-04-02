# 插件开发指北

> 如何开发自定义组件

## 项目模块

dataflow-plugin 为插件开发模块，约定自定义算法组件均维护到该模块下。

## 开发流程

### 实现算法组件接口

自定义实现类，实现 Algorithm 接口。比如下面是一个求方差的算法示例：

```java
package tk.fishfish.dataflow.algorithm;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Serializable;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import tk.fishfish.dataflow.core.Algorithm;
import tk.fishfish.dataflow.core.Argument;
import tk.fishfish.dataflow.util.Validation;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 方差
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Slf4j
public class VarianceAlgorithm implements Algorithm {

    private SparkSession spark;

    @Override
    public String name() {
        // 定义组件名称，全局唯一
        return "ALGORITHM_VARIANCE";
    }

    @Override
    public void compute(Argument argument) {
        // 自定义算法逻辑
        Validation.nonNull(argument.getInput(), "配置 [argument.input] 不能为空");
        Validation.nonNull(argument.getOutput(), "配置 [argument.output] 不能为空");

        String inTable = argument.getInput().getString("table");
        String col = argument.getInput().getString("col");
        Validation.nonEmpty(inTable, "配置 [argument.input.table] 不能为空");
        Validation.nonEmpty(col, "配置 [argument.input.col] 不能为空");

        String outTable = argument.getOutput().getString("table");
        Validation.nonEmpty(outTable, "配置 [argument.output.table] 不能为空");

        inTable = String.format("%s_%s", argument.namespace(), inTable);
        outTable = String.format("%s_%s", argument.namespace(), outTable);
        log.info("计算 {}-{} 方差, 输出表: {}", inTable, col, outTable);

        // 组装求方差列数据
        RDD<Vector> rdd = spark.sqlContext().table(inTable).javaRDD()
                .mapPartitions((FlatMapFunction<Iterator<Row>, Vector>) iterator -> {
                    List<Vector> vectors = new LinkedList<>();
                    iterator.forEachRemaining(row -> {
                        Object value = row.getAs(col);
                        if (value == null) {
                            return;
                        }
                        Vector vector = Vectors.dense(Double.parseDouble(value.toString()));
                        vectors.add(vector);
                    });
                    return vectors.iterator();
                }).rdd();

        // 计算方差
        Double variance;
        rdd.cache();
        try {
            long count = rdd.count();
            if (count == 0) {
                variance = null;
            } else {
                variance = Statistics.colStats(rdd).variance().apply(0);
            }
        } finally {
            rdd.unpersist(false);
        }
        log.info("计算 {}-{} 方差值: {}", inTable, col, variance);

        // 结果注册为临时表
        VarianceRow row = new VarianceRow();
        row.setValue(variance);
        spark.createDataFrame(Collections.singletonList(row), VarianceRow.class).toDF(col).createOrReplaceTempView(outTable);

        // 缓存表
        Dataset<Row> ds = spark.sqlContext().table(outTable);
        ds.cache();
        ds.count();

        // 输出表
        Seq<String> seq = JavaConverters.asScalaIteratorConverter(Collections.singletonList(outTable).iterator()).asScala().toSeq();
        argument.setTables(seq);
    }

    @Override
    public void setSparkSession(SparkSession spark) {
        // 回调 SparkSession 对象
        this.spark = spark;
    }

    @Data
    public static class VarianceRow implements Serializable {

        private Double value;

    }

}
```

### 注册组件

在 src/main/resources/META-INF/services/tk.fishfish.dataflow.core.Task 文件下注册自定义组件的**全类名**

```text
# 自定义实现
tk.fishfish.dataflow.algorithm.VarianceAlgorithm
```

注意：一行一个。

### 描述组件

在 src/main/resources/META-INF/components/组件名称.json 文件下描述组件的信息，比如输入、输出。

```json5
{
  // 算法分组
  "group": "ALGORITHM",
  // 组件名称，全局唯一，与 java 实现类中的 name() 方法一致
  "name": "ALGORITHM_VARIANCE",
  // 中文描述
  "text": "方差",
  "argument": {
    // 输入
    "input": [
      {
        "name": "table",
        "text": "物化视图",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "读取物化视图"
      },
      {
        "name": "col",
        "text": "字段列",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "求方差的字段列，必须是浮点型数据"
      }
    ],
    // 输出
    "output": [
      {
        "name": "table",
        "text": "物化视图",
        "type": "string",
        "component": "input",
        "required": true,
        "remark": "结果物化视图"
      }
    ]
  }
}
```
