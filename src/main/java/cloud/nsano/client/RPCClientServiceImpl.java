package cloud.nsano.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RPCClientServiceImpl implements RPCClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCClientServiceImpl.class);
    private static final String RABBITMQ_RESOURCE = "rabbitmq.properties";

    private final Properties properties;

    public RPCClientServiceImpl(){
        this.properties = this.getProperties();
    }

    @Override
    public String call(String correlationId, String payload) {
        LOGGER.info("RPC client : about to make RPC call using payload : "+ payload);

        String response = null;

        if(payload != null){

            Connection connection = this.getConnection();

            boolean hasConnectionObject = (connection != null);

            LOGGER.info("[x] RPC client : is RabbitMQ connection object present : "+ hasConnectionObject);

            if (hasConnectionObject) {

                boolean isOpened = (connection != null) ? connection.isOpen() : false;

                LOGGER.info("[x] RPC client : is AMQP connection opened : "+ isOpened);

                if(isOpened){
                    Channel channel = null;
                    
                    try {
                        channel = (connection != null) ? connection.createChannel() : null;
                    } catch (IOException e){
                        LOGGER.error("erorr occurred while creating channel from connection. message : "+ e.getMessage());
                    }

                    LOGGER.info("[x] is channel object present : "+ (channel != null));
                    
                    if(channel != null){

                        String replyQueueName = null;
                        
                        try {
                            replyQueueName = channel.queueDeclare().getQueue();
                        } catch (IOException e){
                            LOGGER.error("error occurred while declaring queue to retrieve reply query name. message : "+ e.getMessage());
                        }

                        AMQP.BasicProperties props = new AMQP.BasicProperties
                                .Builder()
                                .correlationId(correlationId)
                                .replyTo(replyQueueName)
                                .build();
                        
                        String requestQueueName = "rpc_queue";

                        try {
                            channel.basicPublish("", requestQueueName, props, payload.getBytes("UTF-8"));
                        } catch (IOException e){
                            LOGGER.error("error occurred while basic publishing to channel. message : "+ e.getMessage());
                        }

                        final CompletableFuture<String> futureRepsonse = new CompletableFuture<>();

                        String ctag = null;
                        
                        try {
                            ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
                                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                                    futureRepsonse.complete(new String(delivery.getBody(), "UTF-8"));
                                }
                            }, consumerTag -> {
                            });
                        } catch (IOException e){
                            LOGGER.error("error occurred while applying basic consume to channel. message : "+ e.getMessage());
                        }

                        String result = null;
                        
                        try {
                            result = futureRepsonse.get();
                        } catch (InterruptedException | ExecutionException e){
                            LOGGER.error("error occurred while process future response. message : "+ e.getMessage());
                        }

                        LOGGER.info("[x] result from RPC compute received : "+ result);

                        try {
                            channel.basicCancel(ctag);
                        } catch (IOException e){
                            LOGGER.error("error occurred while applying basic cancel to channel. message : "+ e.getMessage());
                        }

                    }

                }
            }
        }

        return response;
    }

    public final Properties getProperties(){
        Properties props = null;

        InputStream inputStream = this.getResourceAsStream();

        LOGGER.info("is resource stream found : "+ (inputStream != null));

        if (inputStream != null) {
            props = new Properties();

            try {
                props.load(inputStream);
            } catch (IOException e) {
                LOGGER.error("[x] error occurred while loading rabbit mq properties. message : " + e.getMessage());
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e){
                    LOGGER.error("[x] error occurred while closing rabbit mq properties. message : " + e.getMessage());
                }
            }
        }

        return props;
    }

    InputStream getResourceAsStream() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        return classLoader.getResourceAsStream(RABBITMQ_RESOURCE);
    }

    Integer getPort() {
        Integer port = null;
        try {
            port = Integer.valueOf(this.properties.getProperty("port", "30000"));
        } catch (NumberFormatException e) {
            LOGGER.error("error occurred while converting port from string to int. message : " + e.getMessage());
        }

        return port;
    }

    Connection getConnection() {

        Connection connection = null;

        ConnectionFactory connectionFactory = new ConnectionFactory();

        LOGGER.info("properties loaded : " + this.getProperties());

        String host = this.properties.getProperty("host");
        connectionFactory.setHost(host);

        String username = this.properties.getProperty("username");
        connectionFactory.setUsername(username);

        String password = this.properties.getProperty("password");
        connectionFactory.setPassword(password);

        Integer port = this.getPort();

        connectionFactory.setPort(port);

        try {
            connection = connectionFactory.newConnection();
        } catch (IOException | TimeoutException e) {
            LOGGER.error("error occurred while connecting to RabbitMQ. message : " + e.getMessage());
        }

        LOGGER.info("is connection object present for RabbitMQ. message : " + (connection != null));

        return connection;
    }
    

}
