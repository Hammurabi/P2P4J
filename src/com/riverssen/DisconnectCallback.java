package com.riverssen;

public interface DisconnectCallback {
    void onEvent(Server server, Node node, NodeID nodeID);
}
