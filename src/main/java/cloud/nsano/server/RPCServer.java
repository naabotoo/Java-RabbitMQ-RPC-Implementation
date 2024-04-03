package cloud.nsano.server;

import java.util.Properties;

import com.rabbitmq.client.Connection;


public interface RPCServer {

  Connection getConnection();

  void consume();

  Properties getProperties();
}
