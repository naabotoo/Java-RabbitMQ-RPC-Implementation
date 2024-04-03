package cloud.nsano.server;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartServer.class);

    final static ExecutorService EXECUTOR_SERVICE = Executors.newWorkStealingPool();

    public void start(){
        try {
            EXECUTOR_SERVICE.submit(this.startRPCServer());   
        } catch (Exception e) {
            LOGGER.error("error occurred while starting rpc server. message : "+ e.getMessage());
        }
    }

    Callable<Void> startRPCServer(){
        return () -> {
            throw new UnsupportedOperationException("Not supported yet.");
        };
    }
}
