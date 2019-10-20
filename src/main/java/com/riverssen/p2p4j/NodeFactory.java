package main.java.com.riverssen.p2p4j;

import java.io.IOException;
import java.net.Socket;

public abstract class NodeFactory {
    public abstract Node makeNode(NodeID nodeID, Server server, Socket socket) throws IOException;
    public abstract Node makeConnection(NodeID nodeID, Server server, Socket socket) throws IOException;
}
