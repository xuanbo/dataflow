package tk.fishfish.dataflow.service.impl;

import tk.fishfish.dataflow.dag.Dag;
import tk.fishfish.dataflow.dag.DagExecutor;
import tk.fishfish.dataflow.entity.Execution;
import tk.fishfish.dataflow.entity.Graph;
import tk.fishfish.dataflow.entity.enums.ExecuteStatus;
import tk.fishfish.dataflow.service.ExecutionService;
import tk.fishfish.dataflow.service.GraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.fishfish.json.Json;
import tk.fishfish.mybatis.service.impl.BaseServiceImpl;

import java.util.Date;

/**
 * 流程图
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphServiceImpl extends BaseServiceImpl<Graph> implements GraphService {

    private final ExecutionService executionService;
    private final DagExecutor executor;
    private final Json json;

    @Override
    public void run(String id) {
        Graph graph = this.findById(id);
        if (graph == null) {
            log.warn("流程图不存在: {}", id);
            return;
        }
        // 运行记录
        Execution execution = new Execution();
        execution.setGraphId(id);
        execution.setGraphContent(graph.getContent());
        executionService.insert(execution);
        String executionId = execution.getId();
        // 运行
        String message = executor.run(executionId, Dag.apply(json.read(graph.getContent(), tk.fishfish.dataflow.dag.Graph.class)));
        // 运行结果
        execution = new Execution();
        execution.setId(executionId);
        execution.setStatus(message == null ? ExecuteStatus.SUCCESS : ExecuteStatus.ERROR);
        execution.setMessage(message);
    }

    @Override
    protected void beforeInsert(Graph graph) {
        if (graph.getCreateTime() == null) {
            graph.setCreateTime(new Date());
        }
    }

    @Override
    protected void beforeUpdate(Graph graph) {
        if (graph.getUpdateTime() == null) {
            graph.setUpdateTime(new Date());
        }
    }

}
