package tk.fishfish.dataflow.sdk.domain;

import lombok.Data;

import java.util.Date;

/**
 * 执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
public class ExecutionCondition {

    private String graphId;

    private ExecuteStatus status;

    private Date startCreateTime;

    private Date endCreateTime;

}
