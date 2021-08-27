package tk.fishfish.dataflow.util;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * RestTemplate工厂
 *
 * @author 奔波儿灞
 * @since 1.0.0
 */
public final class RestTemplateFactory {

    private static final int CONNECT_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 15_000;

    public static final RestTemplate DEFAULT_REST_TEMPLATE = createRestTemplate();

    private RestTemplateFactory() {
        throw new IllegalStateException("Utils");
    }

    public static RestTemplate createRestTemplate() {
        return createRestTemplate(CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    public static RestTemplate createRestTemplate(int connectTimeout, int readTimeout) {
        RestTemplate restTemplate = new RestTemplate();
        // 设置UTF-8
        restTemplate.getMessageConverters().add(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        // 设置HttpClient
        restTemplate.setRequestFactory(clientHttpRequestFactory(connectTimeout, readTimeout));
        return restTemplate;
    }

    private static HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        return clientHttpRequestFactory(CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    private static HttpComponentsClientHttpRequestFactory clientHttpRequestFactory(int connectTimeout, int readTimeout) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }

}
