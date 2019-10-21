package com.riverssen.p2p4j;

public interface DisconnectCallback {
    void onEvent(Server server, Node node, NodeID nodeID);
}
