package tk.fishfish.dataflow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.fishfish.dataflow.condition.FlowCondition;
import tk.fishfish.dataflow.entity.Flow;
import tk.fishfish.dataflow.service.FlowService;
import tk.fishfish.mybatis.controller.BaseController;

import java.util.List;

/**
 * 执行流
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/flow")
public class FlowController extends BaseController<Flow> {

    private final FlowService flowService;

    @PostMapping("/list")
    public List<Flow> list(@RequestBody FlowCondition condition) {
        return flowService.query(condition);
    }

}
