package tk.fishfish.dataflow.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tk.fishfish.enums.EnumType;

/**
 * 组件分组
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ComponentGroup implements EnumType {

    OTHER("其他", "0"),

    BASIC("基础", "1"),

    ALGORITHM("算法", "2"),

    ;

    private final String name;
    private final String value;

}
