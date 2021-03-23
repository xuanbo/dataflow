package tk.fishfish.dataflow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.fishfish.dataflow.condition.TaskCondition;
import tk.fishfish.dataflow.entity.Task;
import tk.fishfish.dataflow.service.TaskService;
import tk.fishfish.mybatis.controller.BaseController;

import java.util.List;

/**
 * 节点任务
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/task")
public class TaskController extends BaseController<Task> {

    private final TaskService taskService;

    @PostMapping("/list")
    public List<Task> list(@RequestBody TaskCondition condition) {
        return taskService.query(condition);
    }

}
