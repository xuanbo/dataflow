package tk.fishfish.dataflow.sdk.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
public class Execution {

    private String id;

    private ExecuteStatus status;

    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    private List<Task> tasks;

}
