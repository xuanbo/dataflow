package tk.fishfish.dataflow.sdk.domain;

import lombok.Data;

/**
 * API结果
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
public class ApiResult<T> {

    private boolean success;

    private Integer code;

    private String message;

    private T data;

}
