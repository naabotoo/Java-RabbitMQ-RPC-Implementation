package cloud.nsano.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServiceResponseHandler implements HttpClientResponseHandler<String> {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebServiceResponseHandler.class);

    AtomicInteger statusCode;

    //RabbitMQ consumerTag value
    String ctag;

    public WebServiceResponseHandler(String ctag, AtomicInteger statusCode){
        Objects.requireNonNull(statusCode, "status code is required.");
        Objects.requireNonNull(ctag, "ctag value is required.");
        this.statusCode = statusCode;
        this.ctag = ctag;
    }

    @SuppressWarnings("resource")
    @Override
    public String handleResponse(ClassicHttpResponse chr) throws HttpException, IOException {
        String responseString = null;

        int status = chr.getCode();

        LOGGER.info("[ "+ ctag +" ] web service call response status code : "+ status);

        this.statusCode.set(status);

        if(status >= 200 && status < 300){
            HttpEntity entity = chr.getEntity();

            if(entity != null){
                InputStream inputStream = entity.getContent();

                try {
                    responseString = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
                } catch (Exception e){
                    LOGGER.error("[ "+ ctag +" ] error occurred while processing response string. message : "+ e.getMessage());
                }
            }
        }

        return responseString;
    }
    
}
