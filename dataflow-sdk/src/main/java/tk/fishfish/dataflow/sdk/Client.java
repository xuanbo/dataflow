package tk.fishfish.dataflow.sdk;

import tk.fishfish.dataflow.sdk.domain.ApiResult;
import tk.fishfish.dataflow.sdk.domain.Execution;
import tk.fishfish.dataflow.sdk.domain.ExecutionCondition;
import tk.fishfish.dataflow.sdk.domain.ExecutionParam;
import tk.fishfish.dataflow.sdk.domain.Page;
import tk.fishfish.dataflow.sdk.domain.Query;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;
import java.util.Map;

/**
 * 客户端
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
public interface Client {

    /**
     * 执行DAG
     *
     * @param param 参数
     */
    @Headers({"Content-Type: application/json"})
    @RequestLine("POST /v1/dag/run")
    ApiResult<Void> runDag(ExecutionParam param);

    /**
     * 取消执行
     *
     * @param executionId 执行ID
     */
    @Headers({"Content-Type: application/json"})
    @RequestLine("POST /v1/dag/cancel?executionId={executionId}")
    ApiResult<Void> cancelExecution(String executionId);

    /**
     * 查询执行信息
     *
     * @param executionId 执行ID
     * @return 执行信息
     */
    @Headers({"Content-Type: application/json"})
    @RequestLine("GET /v1/execution/{executionId}/detail")
    ApiResult<Execution> queryExecution(@Param("executionId") String executionId);

    /**
     * 分页查询执行信息
     *
     * @param query 查询
     * @return 执行信息
     */
    @Headers({"Content-Type: application/json"})
    @RequestLine("POST /v1/execution/page")
    ApiResult<Page<Execution>> pageExecution(Query<ExecutionCondition> query);

    /**
     * 查询组件
     *
     * @return 组件
     */
    @Headers({"Content-Type: application/json"})
    @RequestLine("GET /v1/component/list")
    ApiResult<List<Map<String, Object>>> queryComponents();

}
