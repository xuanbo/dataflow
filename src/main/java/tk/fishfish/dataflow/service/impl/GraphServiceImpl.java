package tk.fishfish.dataflow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.fishfish.dataflow.dag.DagExecutor;
import tk.fishfish.dataflow.entity.Graph;
import tk.fishfish.dataflow.service.GraphService;
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

    private final DagExecutor dagExecutor;

    private final Json json;

    @Override
    public void run(String id) {
        Graph graph = this.findById(id);
        if (graph == null) {
            log.warn("流程图不存在: {}", id);
            return;
        }
        dagExecutor.run(id, json.read(graph.getContent(), tk.fishfish.dataflow.dag.Graph.class));
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
