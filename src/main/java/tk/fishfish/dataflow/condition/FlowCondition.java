package tk.fishfish.dataflow.condition;

import lombok.Data;
import tk.fishfish.dataflow.entity.enums.ExecuteStatus;
import tk.fishfish.mybatis.condition.annotation.Eq;

/**
 * 执行流
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
public class FlowCondition {

    @Eq(property = "executionId")
    private String executionId;

    @Eq(property = "status")
    private ExecuteStatus status;

}
