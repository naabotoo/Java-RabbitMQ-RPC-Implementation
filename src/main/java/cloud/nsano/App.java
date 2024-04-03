package cloud.nsano;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.nsano.client.RPCClientServiceImpl;
import cloud.nsano.server.RPCServerRunner;

public class App 
{
    private final static Logger LOGGER = LoggerFactory.getLogger(App.class);
    private final static ExecutorService executorService = Executors.newWorkStealingPool();

    public static void main( String[] args )
    {
        try{
            executorService.submit(new RPCServerRunner());
        } catch (Exception e){
            LOGGER.error("error occurred while message : "+ e.getMessage());
        }

        RPCClientServiceImpl rpcClientServiceImpl = new RPCClientServiceImpl();

        String requestId = UUID.randomUUID().toString();

        LocalDateTime localDateTime = LocalDateTime.now();

        RPCRequest request = new RPCRequest("hello", localDateTime);

        rpcClientServiceImpl.call(requestId, request.toJsonStr());
        
        while (true) {
            
        }
    }
}
