package tk.fishfish.dataflow.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tk.fishfish.mybatis.enums.EnumType;

/**
 * 组件分组
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ComponentGroup implements EnumType {

    BASIC("基础", "0"),

    ALGORITHM("算法", "1"),

    OTHER("其他", "2"),

    ;

    private final String name;
    private final String value;

}
