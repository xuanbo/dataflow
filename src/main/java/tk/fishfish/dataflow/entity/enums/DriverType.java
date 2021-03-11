package tk.fishfish.dataflow.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tk.fishfish.mybatis.enums.EnumType;

/**
 * 驱动类型
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum DriverType implements EnumType {

    MYSQL("MySQL", "0"),

    IOT("Apache IoTDB", "1"),

    ;

    private final String name;
    private final String value;

}
