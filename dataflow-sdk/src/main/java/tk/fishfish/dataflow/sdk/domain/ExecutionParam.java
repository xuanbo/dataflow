package tk.fishfish.dataflow.sdk.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 执行参数
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionParam {

    private String executionId;

    private String graphId;

    private Map<String, Object> context;

    private Graph graph;

    private String callback;

}
