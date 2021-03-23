package tk.fishfish.dataflow.repository;

import tk.fishfish.dataflow.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import tk.fishfish.mybatis.repository.Repository;

/**
 * 节点任务
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Mapper
public interface TaskRepository extends Repository<Task> {
}
