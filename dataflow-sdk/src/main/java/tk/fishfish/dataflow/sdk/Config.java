package tk.fishfish.dataflow.sdk;

import lombok.Builder;
import lombok.Data;

/**
 * 配置
 *
 * @author 奔波儿灞
 * @since 1.0.0
 */
@Data
@Builder
public class Config {

    /**
     * 地址
     */
    private String endpoint;

    /**
     * 申请的client_id
     */
    private String clientId;

    /**
     * 申请的client_secret
     */
    private String clientSecret;

}
