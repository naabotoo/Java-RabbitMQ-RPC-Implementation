package cloud.nsano.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 *
 * @author nanabenyin
 */
public final class RPCServerImpl implements RPCServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCServerImpl.class);

    private final static String RABBITMQ_RESOURCE = "rabbitmq.properties";

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    private final Properties properties;

    public RPCServerImpl() {
        this.properties = this.getProperties();
    }

    @Override
    public Connection getConnection() {

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

    @Override
    public void consume() {
        Connection connection = this.getConnection();

        boolean hasConnectionObject = (connection != null);

        LOGGER.info("[x] is RabbitMQ connection object present : " + hasConnectionObject);

        if (hasConnectionObject) {

            boolean isOpened = (connection != null) ? connection.isOpen() : false;

            LOGGER.info("[x] is AMQP connection opened : " + isOpened);

            if (isOpened) {

                Channel channel = null;

                try {
                    channel = (connection != null) ? connection.createChannel() : null;

                    if (channel != null) {
                        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
                        channel.queuePurge(RPC_QUEUE_NAME);
                        channel.basicQos(1);
                    }
                } catch (IOException ex) {
                    LOGGER.error("[x] error occurred while building channel object for this connection. message : "
                            + ex.getMessage());
                }

                if (channel != null) {
                    LOGGER.info(" [x] Awaiting RPC requests");

                    DeliverCallbackImpl deliverCallback = new DeliverCallbackImpl(channel);

                    try {
                        channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, new RPCServerCancelCallback());
                    } catch (IOException e) {
                        LOGGER.error("[x] error occurred while processing RPC request in consumer. message : "
                                + e.getMessage());
                    }
                }

            }
        }
    }

    @Override
    public Properties getProperties() {
        Properties props = null;

        InputStream inputStream = this.getResourceAsStream();

        LOGGER.info("is resource stream found : " + (inputStream != null));

        if (inputStream != null) {
            props = new Properties();

            try {
                props.load(inputStream);
            } catch (IOException e) {
                LOGGER.error("[x] error occurred while loading rabbit mq properties. message : " + e.getMessage());
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
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
}
