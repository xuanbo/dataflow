package tk.fishfish.dataflow.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tk.fishfish.json.util.JSON;

import java.util.function.Consumer;

/**
 * HTTP工具
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
public final class HttpUtils {

    private HttpUtils() {
        throw new IllegalStateException("Utils");
    }

    public static <T> ResponseEntity<T> post(String url, Object data, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(JSON.write(data), headers);
        return RestTemplateFactory.DEFAULT_REST_TEMPLATE.exchange(url, HttpMethod.POST, entity, responseType);
    }

    public static <T> ResponseEntity<T> post(String url, Object data, Class<T> responseType, Consumer<HttpHeaders> headersConsumer) {
        HttpHeaders headers = new HttpHeaders();
        headersConsumer.accept(headers);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(JSON.write(data), headers);
        return RestTemplateFactory.DEFAULT_REST_TEMPLATE.exchange(url, HttpMethod.POST, entity, responseType);
    }

}
