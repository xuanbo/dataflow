package tk.fishfish.dataflow.condition;

import lombok.Data;
import tk.fishfish.dataflow.entity.enums.DriverType;
import tk.fishfish.mybatis.condition.annotation.Eq;
import tk.fishfish.mybatis.condition.annotation.Like;

import javax.validation.constraints.NotBlank;

/**
 * 数据库
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
public class DatabaseCondition {

    @Like(property = "name", policy = Like.Policy.ALL)
    private String name;

    @Eq(property = "type")
    private DriverType type;

    @Data
    public static class MetaData {

        @NotBlank(groups = ShowTable.class)
        private String name;

        @NotBlank(groups = ShowSql.class)
        private String sql;

    }

    public interface ShowTable {
    }

    public interface ShowSql {
    }

}
