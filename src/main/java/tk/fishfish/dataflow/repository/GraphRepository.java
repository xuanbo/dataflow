package tk.fishfish.dataflow.repository;

import tk.fishfish.dataflow.entity.Graph;
import org.apache.ibatis.annotations.Mapper;
import tk.fishfish.mybatis.repository.Repository;

/**
 * 流程图
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Mapper
public interface GraphRepository extends Repository<Graph> {
}
