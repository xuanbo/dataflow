package tk.fishfish.dataflow.service;

import tk.fishfish.dataflow.entity.Graph;
import org.springframework.scheduling.annotation.Async;
import tk.fishfish.mybatis.service.BaseService;

/**
 * 流程图
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
public interface GraphService extends BaseService<Graph> {

    @Async
    void run(String id);

}
