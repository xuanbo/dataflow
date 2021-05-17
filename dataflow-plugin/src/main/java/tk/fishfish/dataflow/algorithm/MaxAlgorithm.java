package tk.fishfish.dataflow.algorithm;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Serializable;
import tk.fishfish.dataflow.core.Algorithm;
import tk.fishfish.dataflow.core.Argument;
import tk.fishfish.dataflow.util.Validation;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 最大值
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Slf4j
public class MaxAlgorithm implements Algorithm {

    private SparkSession spark;

    @Override
    public String name() {
        // 定义组件名称，全局唯一
        return "ALGORITHM_MAX";
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
        log.info("计算 {}-{} 最大值, 输出表: {}", inTable, col, outTable);

        // 组装求最大值列数据
        JavaRDD<Double> rdd = spark.sqlContext().table(inTable).javaRDD()
                .mapPartitions((FlatMapFunction<Iterator<Row>, Double>) iterator -> {
                    List<Double> values = new LinkedList<>();
                    iterator.forEachRemaining(row -> {
                        Object value = row.getAs(col);
                        if (value == null) {
                            return;
                        }
                        values.add(Double.parseDouble(value.toString()));
                    });
                    return values.iterator();
                });

        // 计算最大值
        Double max;
        rdd.cache();
        try {
            long count = rdd.count();
            if (count == 0) {
                max = null;
            } else {
                max = rdd.reduce(Double::max);
            }
        } finally {
            rdd.unpersist();
        }
        log.info("计算 {}-{} 最大值: {}", inTable, col, max);

        // 结果注册为临时表
        MaxRow row = new MaxRow();
        row.setValue(max);
        spark.createDataFrame(Collections.singletonList(row), MaxRow.class).toDF(col).createOrReplaceTempView(outTable);

        // 缓存表
        Dataset<Row> ds = spark.sqlContext().table(outTable);
        ds.cache();
        ds.count();
    }

    @Override
    public void setSparkSession(SparkSession spark) {
        // 回调 SparkSession 对象
        this.spark = spark;
    }

    @Data
    public static class MaxRow implements Serializable {

        private Double value;

    }

}