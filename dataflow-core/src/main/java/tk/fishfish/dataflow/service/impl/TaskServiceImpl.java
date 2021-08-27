package tk.fishfish.dataflow.service.impl;

import tk.fishfish.dataflow.entity.Task;
import tk.fishfish.dataflow.service.TaskService;
import org.springframework.stereotype.Service;
import tk.fishfish.mybatis.service.impl.BaseServiceImpl;

import java.util.Date;

/**
 * 节点任务
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Service
public class TaskServiceImpl extends BaseServiceImpl<Task> implements TaskService {

    @Override
    public void beforeInsert(Task task) {
        if (task.getCreateTime() == null) {
            task.setCreateTime(new Date());
        }
    }

    @Override
    public void beforeUpdate(Task task) {
        if (task.getUpdateTime() == null) {
            task.setUpdateTime(new Date());
        }
    }

}
