package tk.fishfish.dataflow.service.impl;

import tk.fishfish.dataflow.entity.Execution;
import tk.fishfish.dataflow.entity.Task;
import tk.fishfish.dataflow.repository.TaskRepository;
import tk.fishfish.dataflow.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tk.fishfish.mybatis.service.impl.BaseServiceImpl;

import java.util.Date;
import java.util.List;

/**
 * 执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ExecutionServiceImpl extends BaseServiceImpl<Execution> implements ExecutionService {

    private final TaskRepository taskRepository;

    @Override
    public Execution detail(String id) {
        Execution execution = findById(id);
        if (execution == null) {
            return null;
        }
        List<Task> tasks = taskRepository.findByExecutionId(id);
        execution.setTasks(tasks);
        return execution;
    }

    @Override
    public void beforeInsert(Execution execution) {
        if (execution.getCreateTime() == null) {
            execution.setCreateTime(new Date());
        }
    }

    @Override
    public void beforeUpdate(Execution execution) {
        if (execution.getUpdateTime() == null) {
            execution.setUpdateTime(new Date());
        }
    }

}
