package com.riverssen.p2p4j;

public interface MessageReceivedCallback {
    void onEvent(Server server, Node node, byte packetData[]);
}
