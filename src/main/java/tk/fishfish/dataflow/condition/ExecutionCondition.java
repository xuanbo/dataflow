package tk.fishfish.dataflow.condition;

import tk.fishfish.dataflow.entity.enums.ExecuteStatus;
import lombok.Data;
import tk.fishfish.mybatis.condition.annotation.Eq;
import tk.fishfish.mybatis.condition.annotation.Gte;
import tk.fishfish.mybatis.condition.annotation.Lt;

import java.util.Date;

/**
 * 执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
public class ExecutionCondition {

    @Eq(property = "graphId")
    private String graphId;

    @Eq(property = "status")
    private ExecuteStatus status;

    @Gte(property = "createTime")
    private Date startCreateTime;

    @Lt(property = "createTime")
    private Date endCreateTime;

}
