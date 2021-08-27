package tk.fishfish.dataflow.sdk.json;

import tk.fishfish.dataflow.sdk.domain.ExecuteStatus;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import java.io.IOException;
import java.util.Map;

/**
 * 反序列化
 *
 * @author 奔波儿灞
 * @version 1.0.0
 */
public class ExecuteStatusDeserializer extends JsonDeserializer<ExecuteStatus> {

    @SuppressWarnings("unchecked")
    public ExecuteStatus deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        JsonToken t = parser.getCurrentToken();
        String value = null;
        if (t == JsonToken.VALUE_STRING) {
            value = parser.getValueAsString();
        } else if (t == JsonToken.VALUE_NUMBER_INT) {
            value = parser.getIntValue() + "";
        } else if (t == JsonToken.START_OBJECT) {
            // 对象 {"name":"男","value":"0"}
            Map<String, Object> map = parser.readValueAs(Map.class);
            Object ori = map.get("value");
            if (ori != null) {
                value = ori.toString();
            }
        } else {
            throw MismatchedInputException.from(parser, ExecuteStatus.class, "枚举反序列化不匹配: " + parser.getValueAsString());
        }
        ExecuteStatus[] values = ExecuteStatus.values();
        for (ExecuteStatus status : values) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

}
