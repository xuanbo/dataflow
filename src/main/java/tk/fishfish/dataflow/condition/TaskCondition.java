package tk.fishfish.dataflow.condition;

import tk.fishfish.dataflow.entity.enums.ExecuteStatus;
import lombok.Data;
import tk.fishfish.mybatis.condition.annotation.Eq;
import tk.fishfish.mybatis.condition.annotation.Like;

/**
 * 节点任务
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
public class TaskCondition {

    @Eq(property = "executionId")
    private String executionId;

    @Like(property = "nodeId", policy = Like.Policy.ALL)
    private String nodeId;

    @Like(property = "nodeName", policy = Like.Policy.ALL)
    private String nodeName;

    @Like(property = "nodeText", policy = Like.Policy.ALL)
    private String nodeText;

    @Eq(property = "status")
    private ExecuteStatus status;

}
