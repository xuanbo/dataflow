package tk.fishfish.dataflow.sdk;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.Logger;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端工厂
 *
 * @author 奔波儿灞
 * @since 1.0.0
 */
@Slf4j
public final class ClientFactory {

    @Getter
    private final ObjectMapper objectMapper;

    public ClientFactory() {
        this.objectMapper = defaultObjectMapper();
    }

    public ClientFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Client newClient(Config config) {
        return Feign.builder()
                .logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .client(new OkHttpClient())
                .decoder(new JacksonDecoder(objectMapper))
                .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
                .target(Client.class, config.getEndpoint());
    }

    private ObjectMapper defaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

}
