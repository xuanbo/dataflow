package tk.fishfish.dataflow.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tk.fishfish.mybatis.enums.EnumType;

/**
 * 执行状态
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ExecuteStatus implements EnumType {

    RUNNING("运行中", "0"),

    SUCCESS("成功", "1"),

    ERROR("失败", "2"),

    ;

    private final String name;
    private final String value;

}
