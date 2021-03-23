package tk.fishfish.dataflow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.fishfish.dataflow.condition.GraphCondition;
import tk.fishfish.dataflow.entity.Graph;
import tk.fishfish.dataflow.service.GraphService;
import tk.fishfish.mybatis.controller.BaseController;
import tk.fishfish.mybatis.domain.Page;
import tk.fishfish.mybatis.domain.Query;

/**
 * 流程图
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/graph")
public class GraphController extends BaseController<Graph> {

    private final GraphService graphService;

    @PostMapping("/page")
    public Page<Graph> page(@RequestBody Query<GraphCondition> query) {
        return graphService.page(query.getCondition(), query.getPage());
    }

    @PostMapping("/{id}/run")
    public void run(@PathVariable String id) {
        graphService.run(id);
    }

}
