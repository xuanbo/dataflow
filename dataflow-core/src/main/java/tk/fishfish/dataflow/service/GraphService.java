package tk.fishfish.dataflow.service;

import tk.fishfish.dataflow.entity.Graph;
import tk.fishfish.mybatis.service.BaseService;

import java.util.Map;

/**
 * 流程图
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
public interface GraphService extends BaseService<Graph> {

    void run(String id, Map<String, Object> context);

}
