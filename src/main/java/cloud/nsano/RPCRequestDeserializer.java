package cloud.nsano;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class RPCRequestDeserializer extends JsonDeserializer<RPCRequest> {
    
    @Override
    public RPCRequest deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JacksonException {
        JsonNode node = jp.getCodec().readTree(jp);

        String word = node.has("word") ? ((!node.get("word").isNull()) ? node.get("word").asText() : null) : null;
        
        String requestAsString = node.has("requestedAt") ? ((!node.get("requestedAt").isNull()) ? node.get("requestedAt").asText() : null) : null;

        LocalDateTime requestAt = (requestAsString != null) ? LocalDateTime.parse(requestAsString, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;

        RPCRequest request = new RPCRequest();
        request.setWord(word);
        request.setRequestAt(requestAt);
        return request;
    }
    
}
