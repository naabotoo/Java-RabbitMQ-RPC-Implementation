package cloud.nsano.server;

import java.io.IOException;

import com.rabbitmq.client.CancelCallback;

public class RPCServerCancelCallback implements CancelCallback {

    @Override
    public void handle(String consumerTag) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }

        
}
