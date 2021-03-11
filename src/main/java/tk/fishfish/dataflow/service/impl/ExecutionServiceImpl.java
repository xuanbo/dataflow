package tk.fishfish.dataflow.service.impl;

import org.springframework.stereotype.Service;
import tk.fishfish.dataflow.entity.Database;
import tk.fishfish.dataflow.entity.Execution;
import tk.fishfish.dataflow.service.ExecutionService;
import tk.fishfish.mybatis.service.BaseService;
import tk.fishfish.mybatis.service.impl.BaseServiceImpl;

import java.util.Date;

/**
 * 执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Service
public class ExecutionServiceImpl extends BaseServiceImpl<Execution> implements ExecutionService {

    @Override
    protected void beforeInsert(Execution execution) {
        if (execution.getCreateTime() == null) {
            execution.setCreateTime(new Date());
        }
    }

    @Override
    protected void beforeUpdate(Execution execution) {
        if (execution.getUpdateTime() == null) {
            execution.setUpdateTime(new Date());
        }
    }

}
