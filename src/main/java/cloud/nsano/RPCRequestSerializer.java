package cloud.nsano;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class RPCRequestSerializer extends JsonSerializer<RPCRequest> {
    
    @Override
    public void serialize(RPCRequest t, JsonGenerator jg, SerializerProvider sp) throws IOException {
        jg.writeStartObject();

        String word = t.getWord();

        jg.writeStringField("word", word);

        String formatedRequestedAt = t.getformatedRequestedAt();

        jg.writeStringField("requestedAt", formatedRequestedAt);

        jg.writeEndObject();
    }
    
}
