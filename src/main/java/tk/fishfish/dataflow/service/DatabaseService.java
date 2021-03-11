package tk.fishfish.dataflow.service;

import scala.collection.Seq;
import tk.fishfish.dataflow.database.Table;
import tk.fishfish.dataflow.entity.Database;
import tk.fishfish.mybatis.service.BaseService;

/**
 * 数据库
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
public interface DatabaseService extends BaseService<Database> {

    void ping(Database database);

    Seq<String> tables(String id);

    Table showTable(String id, String name);

    Table showSql(String id, String sql);

}
