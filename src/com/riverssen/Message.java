package com.riverssen;

import java.util.concurrent.ExecutionException;

public abstract class Message {
    private Packet packet;

    protected Message(Packet packet) {
        this.packet = packet;
    }
    /**
     * Sends information to the node
     * and waits for a reply.
     * @return A replymessage containing data and an error code.
     */
    public abstract <ReturnType, ResponseCode extends Enum<ResponseCode>> ResponsePacket<ReturnType, ResponseCode> sendGetReply(Node node, long timeOut) throws ExecutionException, InterruptedException;
//    public abstract <ReturnType, ResponseCode extends Enum<ResponseCode>> ResponsePacket<ReturnType, ResponseCode>[] sendGetReply(Server network) throws ExecutionException, InterruptedException;

    /**
     * Sends data to the node and returns.
     */
    public boolean send(Node node) {
        return node.send(packet);
    }

    public Packet getPacket() { return packet; }
}
