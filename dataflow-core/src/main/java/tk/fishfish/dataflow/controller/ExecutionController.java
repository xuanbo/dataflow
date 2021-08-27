package tk.fishfish.dataflow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.fishfish.dataflow.condition.ExecutionCondition;
import tk.fishfish.dataflow.entity.Execution;
import tk.fishfish.dataflow.service.ExecutionService;
import tk.fishfish.mybatis.controller.BaseController;
import tk.fishfish.mybatis.domain.Page;
import tk.fishfish.mybatis.domain.Query;

/**
 * 执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/execution")
public class ExecutionController extends BaseController<Execution> {

    private final ExecutionService executionService;

    @PostMapping("/page")
    public Page<Execution> page(@RequestBody Query<ExecutionCondition> query) {
        return executionService.page(query.getCondition(), query.getPage());
    }

    @GetMapping("/{id}/detail")
    public Execution detail(@PathVariable String id) {
        return executionService.detail(id);
    }

}
