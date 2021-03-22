package tk.fishfish.dataflow.repository;

import tk.fishfish.dataflow.entity.Execution;
import org.apache.ibatis.annotations.Mapper;
import tk.fishfish.mybatis.repository.Repository;

/**
 * 执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Mapper
public interface ExecutionRepository extends Repository<Execution> {
}
