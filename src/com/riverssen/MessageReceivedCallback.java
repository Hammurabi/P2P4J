package com.riverssen;

public interface MessageReceivedCallback {
    void onEvent(Server server, Node node, byte packetData[]);
}
