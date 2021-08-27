package tk.fishfish.dataflow.sdk.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 节点任务
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
public class Task {

    private String id;

    private String executionId;

    private String nodeId;

    private String nodeName;

    private String nodeText;

    private ExecuteStatus status;

    private String message;

    private String data;

    private Long numbers;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
