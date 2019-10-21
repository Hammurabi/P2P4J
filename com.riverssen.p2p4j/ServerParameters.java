import java.util.concurrent.ExecutorService;

public abstract class ServerParameters {
    private final int               maxConnections;
    private final int               maxBytesSend;
    private final int               maxBytesReceive;
    private final int               port;
    private final ExecutorService   threadPool;
    private final MessageReceivedCallback requestCallback;
    private final NodeFactory       nodeFactory;
    private final ConnectionRequestCallback connectionRequestCallback;
    private final DisconnectCallback disconnectCallback;

    public ServerParameters(final int maxSend, final int maxReceive, final int maxConnections, final int port, final ExecutorService threadPool, final MessageReceivedCallback requestCallback, final ConnectionRequestCallback connectionRequestCallback, final DisconnectCallback disconnectCallback, final NodeFactory nodeFactory)
    {
        this.maxConnections = maxConnections;
        this.maxBytesSend   = maxSend;
        this.maxBytesReceive= maxReceive;
        this.threadPool     = threadPool;
        this.port           = port;
        this.requestCallback = requestCallback;
        this.nodeFactory    = nodeFactory;
        this.connectionRequestCallback = connectionRequestCallback;
        this.disconnectCallback = disconnectCallback;
    }

    public final int getMaxConnections()
    {
        return maxConnections;
    }

    public final ExecutorService getThreadPool()
    {
        return threadPool;
    }

    public final int getPort() { return port; }

    public final int getMaxBytesSend()
    {
        return maxBytesSend;
    }

    public int getMaxBytesReceive() {
        return maxBytesReceive;
    }

    public MessageReceivedCallback getMessageReceivedCallback() {
        return requestCallback;
    }

    public abstract boolean getKeepAlive();

    public NodeFactory getFactory() {
        return nodeFactory;
    }

    public ConnectionRequestCallback getConnectionRequestCallback() {
        return connectionRequestCallback;
    }

    public DisconnectCallback getDisconnectCallback() {
        return disconnectCallback;
    }
}
