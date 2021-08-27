package tk.fishfish.dataflow.sdk.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页数据
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
public class Page<T> implements Serializable {

    private Integer page;
    private Integer size;
    private Long total;
    private List<T> data;

}
