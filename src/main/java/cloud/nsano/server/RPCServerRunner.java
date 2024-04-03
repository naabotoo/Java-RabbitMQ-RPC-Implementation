package cloud.nsano.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RPCServerRunner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCServerRunner.class);

    @Override
    public void run() {        
        LOGGER.info("RPC Server listener thread started...");
        RPCServerImpl rpcServerImpl = new RPCServerImpl();
        rpcServerImpl.consume();
    }

}