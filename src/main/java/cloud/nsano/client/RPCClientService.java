package cloud.nsano.client;

public interface RPCClientService {
    String call(String correlationId, String payload);
}
