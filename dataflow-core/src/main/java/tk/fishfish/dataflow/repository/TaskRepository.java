package tk.fishfish.dataflow.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.fishfish.dataflow.entity.Task;
import tk.fishfish.mybatis.repository.Repository;

import java.util.List;

/**
 * 节点任务
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Mapper
public interface TaskRepository extends Repository<Task> {

    @Select("SELECT * FROM df_task WHERE execution_id = #{executionId} ORDER BY create_time DESC")
    List<Task> findByExecutionId(@Param("executionId") String executionId);

}
