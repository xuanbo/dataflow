package tk.fishfish.dataflow.sdk.json;

import tk.fishfish.dataflow.sdk.domain.ExecuteStatus;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 序列化
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
public class ExecuteStatusSerializer extends JsonSerializer<ExecuteStatus> {

    public void serialize(ExecuteStatus value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", value.getName());
        gen.writeStringField("value", value.getValue());
        gen.writeEndObject();
    }

}
