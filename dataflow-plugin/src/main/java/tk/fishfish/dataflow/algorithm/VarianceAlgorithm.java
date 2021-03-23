package tk.fishfish.dataflow.algorithm;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.function.Function;
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
import java.util.Objects;

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
        RDD<Vector> rdd = spark.sqlContext().table(inTable).javaRDD().map((Function<Row, Vector>) row -> {
            Object value = row.getAs(col);
            if (value == null) {
                return null;
            }
            return Vectors.dense(Double.parseDouble(value.toString()));
        }).filter((Function<Vector, Boolean>) Objects::nonNull).rdd();

        // 计算方差
        double variance = Statistics.colStats(rdd).variance().apply(0);
        log.info("计算 {}-{} 方差值: {}", inTable, col, variance);

        // 结果注册为临时表
        VarianceRow row = new VarianceRow();
        row.setValue(variance);
        spark.createDataFrame(Collections.singletonList(row), VarianceRow.class).toDF(col).createOrReplaceTempView(outTable);

        // 缓存
        spark.sqlContext().table(outTable).cache();
        spark.sqlContext().table(outTable).count();

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