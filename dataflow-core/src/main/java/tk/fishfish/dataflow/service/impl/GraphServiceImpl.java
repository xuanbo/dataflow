package tk.fishfish.dataflow.service.impl;

import tk.fishfish.dataflow.dag.DagExecutor;
import tk.fishfish.dataflow.dag.ExecutionParam;
import tk.fishfish.dataflow.entity.Graph;
import tk.fishfish.dataflow.service.GraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import scala.collection.JavaConversions;
import tk.fishfish.json.util.JSON;
import tk.fishfish.mybatis.service.impl.BaseServiceImpl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    private final DagExecutor executor;

    @Override
    public void run(String id, Map<String, Object> context) {
        Graph graph = this.findById(id);
        if (graph == null) {
            log.warn("流程图不存在: {}", id);
            return;
        }
        String executionId = generateId();
        if (context == null) {
            context = new HashMap<>();
        }
        // 运行
        executor.run(new ExecutionParam(
                executionId,
                graph.getId(),
                JavaConversions.mapAsScalaMap(context),
                JSON.read(graph.getContent(), tk.fishfish.dataflow.dag.Graph.class),
                null
        ));
    }

    @Override
    public void beforeInsert(Graph graph) {
        if (graph.getCreateTime() == null) {
            graph.setCreateTime(new Date());
        }
    }

    @Override
    public void beforeUpdate(Graph graph) {
        if (graph.getUpdateTime() == null) {
            graph.setUpdateTime(new Date());
        }
    }

}
