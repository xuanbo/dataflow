package tk.fishfish.dataflow.sdk.domain;

import tk.fishfish.dataflow.sdk.json.ExecuteStatusDeserializer;
import tk.fishfish.dataflow.sdk.json.ExecuteStatusSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 执行状态
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
@JsonSerialize(using = ExecuteStatusSerializer.class)
@JsonDeserialize(using = ExecuteStatusDeserializer.class)
public enum ExecuteStatus {

    RUNNING("运行中", "0"),

    SUCCESS("成功", "1"),

    ERROR("失败", "2"),

    ;

    private final String name;
    private final String value;

}
