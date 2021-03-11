package tk.fishfish.dataflow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scala.collection.Seq;
import tk.fishfish.dataflow.condition.DatabaseCondition;
import tk.fishfish.dataflow.database.Table;
import tk.fishfish.dataflow.entity.Database;
import tk.fishfish.dataflow.service.DatabaseService;
import tk.fishfish.mybatis.controller.BaseController;

import java.util.List;

/**
 * 数据库
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/database")
public class DatabaseController extends BaseController<Database> {

    private final DatabaseService databaseService;

    @PostMapping("/list")
    public List<Database> list(@RequestBody DatabaseCondition condition) {
        return databaseService.query(condition);
    }

    @PostMapping("/ping")
    public void ping(@RequestBody Database database) {
        databaseService.ping(database);
    }

    @PostMapping("/{id}/tables")
    public Seq<String> tables(@PathVariable String id) {
        return databaseService.tables(id);
    }

    @PostMapping("/{id}/table-struct")
    public Table showTable(
            @PathVariable String id,
            @Validated(DatabaseCondition.ShowTable.class) @RequestBody DatabaseCondition.MetaData metaData
    ) {
        return databaseService.showTable(id, metaData.getName());
    }

    @PostMapping("/{id}/sql-struct")
    public Table showSql(
            @PathVariable String id,
            @Validated(DatabaseCondition.ShowSql.class) @RequestBody DatabaseCondition.MetaData metaData
    ) {
        return databaseService.showSql(id, metaData.getSql());
    }

}
