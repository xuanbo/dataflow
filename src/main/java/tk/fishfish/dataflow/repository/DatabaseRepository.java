package tk.fishfish.dataflow.repository;

import org.apache.ibatis.annotations.Mapper;
import tk.fishfish.dataflow.entity.Database;
import tk.fishfish.mybatis.repository.Repository;

/**
 * 数据库
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Mapper
public interface DatabaseRepository extends Repository<Database> {
}
