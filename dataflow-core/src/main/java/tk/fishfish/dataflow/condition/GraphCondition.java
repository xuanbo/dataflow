package tk.fishfish.dataflow.condition;

import lombok.Data;
import tk.fishfish.mybatis.condition.annotation.Gte;
import tk.fishfish.mybatis.condition.annotation.Like;
import tk.fishfish.mybatis.condition.annotation.Lt;

import java.util.Date;

/**
 * 流程图
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Data
public class GraphCondition {

    @Like(property = "name", policy = Like.Policy.ALL)
    private String name;

    @Gte(property = "createTime")
    private Date startCreateTime;

    @Lt(property = "createTime")
    private Date endCreateTime;

}
