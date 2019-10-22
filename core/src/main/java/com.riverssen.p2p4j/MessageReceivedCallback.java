package com.riverssen.p2p4j;

public interface MessageReceivedCallback {
    /**
     * @param server the server (group).
     * @param node the author of this message.
     * @param packetData the bytes contained in this message.
     * @return Hashcode of packetData excluding all
     *         message codes.
     */
    byte[] onEvent(Server server, Node node, byte packetData[]);
}
