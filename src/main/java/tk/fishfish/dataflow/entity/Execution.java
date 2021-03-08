package tk.fishfish.dataflow.entity;

import tk.fishfish.dataflow.entity.enums.ExecuteStatus;
import tk.fishfish.mybatis.entity.Entity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * 执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Table(name = "df_execution")
public class Execution extends Entity {

    private String graphId;

    @Column(name = "status")
    private ExecuteStatus status;

    private Date createTime;

    private Date updateTime;

    public String getGraphId() {
        return graphId;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    public ExecuteStatus getStatus() {
        return status;
    }

    public void setStatus(ExecuteStatus status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
