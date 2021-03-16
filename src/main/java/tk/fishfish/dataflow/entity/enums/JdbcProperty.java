package tk.fishfish.dataflow.entity.enums;

import lombok.RequiredArgsConstructor;

/**
 * Jdbc参数
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@RequiredArgsConstructor
public enum JdbcProperty {

    USER("user"),
    PASSWORD("password"),
    URL("url"),
    CATALOG("catalog"),
    SCHEMA("schema"),

    BATCH("batch"),

    ;

    private final String key;

    public String key() {
        return key;
    }

}
