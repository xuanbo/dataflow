package tk.fishfish.dataflow.algorithm;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 数量
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Slf4j
public class CountAlgorithm implements Algorithm {

    private SparkSession spark;

    @Override
    public String name() {
        // 定义组件名称，全局唯一
        return "ALGORITHM_COUNT";
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
        log.info("计算 {}-{} 数量, 输出表: {}", inTable, col, outTable);

        // 组装求数量列数据
        long count = spark.sqlContext().table(inTable).javaRDD().count();
        log.info("计算 {}-{} 数量: {}", inTable, col, count);

        // 结果注册为临时表
        CountRow row = new CountRow();
        row.setValue(count);
        spark.createDataFrame(Collections.singletonList(row), CountRow.class).toDF(col).createOrReplaceTempView(outTable);

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
    public static class CountRow implements Serializable {

        private Long value;

    }

}