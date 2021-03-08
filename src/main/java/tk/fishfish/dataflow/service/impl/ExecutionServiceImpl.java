package tk.fishfish.dataflow.service.impl;

import org.springframework.stereotype.Service;
import tk.fishfish.dataflow.entity.Execution;
import tk.fishfish.dataflow.service.ExecutionService;
import tk.fishfish.mybatis.service.BaseService;
import tk.fishfish.mybatis.service.impl.BaseServiceImpl;

/**
 * 执行
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
@Service
public class ExecutionServiceImpl extends BaseServiceImpl<Execution> implements ExecutionService {
}
