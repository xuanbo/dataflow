package tk.fishfish.dataflow.service.impl;

import org.springframework.stereotype.Service;
import tk.fishfish.dataflow.entity.Execution;
import tk.fishfish.dataflow.entity.Flow;
import tk.fishfish.dataflow.service.FlowService;
import tk.fishfish.mybatis.service.impl.BaseServiceImpl;

import java.util.Date;

/**
 * 执行流
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Service
public class FlowServiceImpl extends BaseServiceImpl<Flow> implements FlowService {

    @Override
    protected void beforeInsert(Flow flow) {
        if (flow.getCreateTime() == null) {
            flow.setCreateTime(new Date());
        }
    }

    @Override
    protected void beforeUpdate(Flow flow) {
        if (flow.getUpdateTime() == null) {
            flow.setUpdateTime(new Date());
        }
    }

}
