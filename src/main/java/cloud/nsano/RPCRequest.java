package cloud.nsano;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = RPCRequestSerializer.class)
@JsonDeserialize(using = RPCRequestDeserializer.class)
public class RPCRequest {
    private final static Logger LOGGER = LoggerFactory.getLogger(RPCRequest.class);

    String word;
    LocalDateTime requestAt;

    public RPCRequest(){}

    public RPCRequest(String word, LocalDateTime requestAt){
        this.word = word;
        this.requestAt = requestAt;
    }

    public String getWord(){
        return this.word;
    }

    public void setWord(String word){
        this.word = word;
    }

    public LocalDateTime getRequestAt(){
        return this.requestAt;
    }

    public void setRequestAt(LocalDateTime requestAt){
        this.requestAt = requestAt;
    }

    @Override
    public String toString(){
        String formatedRequestAt = this.getformatedRequestedAt();
        return "word : "+ this.word + ", request at : "+ formatedRequestAt;
    }

    public String getformatedRequestedAt(){
        return (this.requestAt != null) ? this.requestAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    public String toJsonStr(){
        String value = null;

        ObjectMapper mapper = new ObjectMapper();

        try {
            value = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOGGER.error("error occurred while converting RPCRequest to JSON string. message : "+ e.getMessage());
        }

        return value;
    }

    public static RPCRequest fromString(String consumerTag, String payload){
        RPCRequest request = null;

        ObjectMapper mapper = new ObjectMapper();

        try {
            request = mapper.readValue(payload, RPCRequest.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("error occurred while converting string to RPCRequest string. message : "+ e.getMessage());
        }

        return request;
    }

}
