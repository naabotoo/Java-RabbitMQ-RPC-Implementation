package cloud.nsano.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

import cloud.nsano.RPCRequest;
import cloud.nsano.RPCResponse;

public class DeliverCallbackImpl implements DeliverCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeliverCallbackImpl.class);

    Channel channel;

    public DeliverCallbackImpl(Channel channel){
        Objects.requireNonNull(channel, "channel object is required.");
        this.channel = channel;
    }

    @Override
    public void handle(String consumerTag, Delivery message) throws IOException {

        String correlationId = message.getProperties().getCorrelationId();

        String replyTo = message.getProperties().getReplyTo();

        LOGGER.info("[ "+ consumerTag +" ] receipted message on with correlation id : "+ correlationId + " reply to : "+ replyTo);


        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(correlationId)
                    .replyTo(replyTo)
                    .build();

        String response = "";

        byte[] body = message.getBody();

        if (body != null) {
            String bodyAsStr = null;
            
            try {
                bodyAsStr = new String(body, "UTF-8");
            } catch (UnsupportedEncodingException e){
                LOGGER.error("[ "+ consumerTag +" ] error occurred while converting message body to string. messag : "+ e.getMessage());
            }

            LOGGER.info("[ " + consumerTag + " ] correlation id : "+ correlationId +" received message body as string : "+ bodyAsStr);

            if(bodyAsStr != null){
                RPCRequest request = RPCRequest.fromString(consumerTag, bodyAsStr);

                LOGGER.info("[ "+ consumerTag + " ] rpc request object present : "+((request != null) ? request.toString() : "none"));

                if(request != null){
                    RPCResponse rpcResponse = this.sendRequest(consumerTag, request);

                    try {
                        response = rpcResponse.toJson();
                    } catch (Exception e){
                        LOGGER.error("[ "+ consumerTag +" ] error occurred while processing response to RPC request with correlation id : "+ correlationId + " message : "+ e.getMessage());
                    } finally {
                        channel.basicPublish("", replyTo, replyProps, response.getBytes("UTF-8"));
                        channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                    }
                }
            }
        }
        
    }

    RPCResponse sendRequest(String consumerTag, RPCRequest request){
        RPCResponse response = null;

        if (request != null) {
            CloseableHttpClient httpclient = HttpClients.createDefault();

            String word = request.getWord();

            //TODO: move this implementation into a application.properties
            HttpGet httpGet = new HttpGet("https://api.dictionaryapi.dev/api/v2/entries/en/"+ word);
                        
            try {
                AtomicInteger statusCode = new AtomicInteger(400);

                HttpClientResponseHandler<String> responseHandler = new WebServiceResponseHandler(consumerTag, statusCode);

                String contentAsString = httpclient.execute(httpGet, responseHandler);
                
                int responseStatus = statusCode.get();

                LOGGER.info("[ "+ consumerTag +" ] response status code : "+ responseStatus);

                if(responseStatus == 200){

                    LOGGER.info("[ "+ consumerTag +" ] response : "+ contentAsString);

                    response = new RPCResponse(request, contentAsString);
                }
            } catch (IOException | UnsupportedOperationException e){
                LOGGER.warn("[ "+ consumerTag +" ] error occurred while message : "+ e.getMessage());
            }
        }

        return response;
    }

}