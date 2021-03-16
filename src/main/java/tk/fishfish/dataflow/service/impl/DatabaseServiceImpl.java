package tk.fishfish.dataflow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import scala.collection.Seq;
import tk.fishfish.dataflow.database.DataHubFactory;
import tk.fishfish.dataflow.database.Table;
import tk.fishfish.dataflow.entity.Database;
import tk.fishfish.dataflow.entity.enums.JdbcProperty;
import tk.fishfish.dataflow.service.DatabaseService;
import tk.fishfish.dataflow.util.Properties;
import tk.fishfish.dataflow.util.StringUtils;
import tk.fishfish.mybatis.service.impl.BaseServiceImpl;
import tk.fishfish.rest.BizException;

import java.util.Date;
import java.util.Optional;

/**
 * 数据库
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class DatabaseServiceImpl extends BaseServiceImpl<Database> implements DatabaseService {

    @Override
    public void ping(Database database) {
        Properties props = props(database);
        String message = DataHubFactory.create(database.getType(), props).ping();
        if (StringUtils.isNotEmpty(message)) {
            throw BizException.of(-1, message);
        }
    }

    @Override
    public Seq<String> tables(String id) {
        Database database = Optional.ofNullable(findById(id))
                .orElseThrow(() -> BizException.of(400, "数据库ID不存在"));
        Properties props = props(database);
        return DataHubFactory.create(database.getType(), props).tables();
    }

    @Override
    public Table showTable(String id, String name) {
        Database database = Optional.ofNullable(findById(id))
                .orElseThrow(() -> BizException.of(400, "数据库ID不存在"));
        Properties props = props(database);
        return DataHubFactory.create(database.getType(), props).showTable(name);
    }

    @Override
    public Table showSql(String id, String sql) {
        Database database = Optional.ofNullable(findById(id))
                .orElseThrow(() -> BizException.of(400, "数据库ID不存在"));
        Properties props = props(database);
        return DataHubFactory.create(database.getType(), props).showSql(sql);
    }

    private Properties props(Database database) {
        return new Properties()
                .option(JdbcProperty.URL.key(), database.getUrl())
                .option(JdbcProperty.USER.key(), database.getUser())
                .option(JdbcProperty.PASSWORD.key(), database.getPassword())
                .option(JdbcProperty.CATALOG.key(), database.getCatalog())
                .option(JdbcProperty.SCHEMA.key(), database.getSchema());
    }

    @Override
    protected void beforeInsert(Database database) {
        if (database.getCreateTime() == null) {
            database.setCreateTime(new Date());
        }
    }

    @Override
    protected void beforeUpdate(Database database) {
        if (database.getUpdateTime() == null) {
            database.setUpdateTime(new Date());
        }
    }

}
