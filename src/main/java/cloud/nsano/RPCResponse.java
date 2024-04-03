package cloud.nsano;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RPCResponse {
    private final static Logger LOGGER = LoggerFactory.getLogger(RPCResponse.class);

    RPCRequest request;
    String response;

    public RPCResponse(RPCRequest request, String response){
        this.request = request;
        this.response = response;
    }

    public void setRequest(RPCRequest request){
        this.request = request;
    }

    public RPCRequest getRequest(){
        return this.request;
    }

    public String getResponse(){
        return this.response;
    }

    public void setResponse(String response){
        this.response = response;
    }

    public String toJson(){
        String value = null;

        ObjectMapper mapper = new ObjectMapper();

        try {
            value = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOGGER.error("error occurred while converting RPCResponse to JSON string. message : "+ e.getMessage());
        } 

        return value;
    }
}
