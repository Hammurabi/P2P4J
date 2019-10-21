package com.riverssen.p2p4j;

public abstract class MessageFactory<EPacketType extends Enum<EPacketType>, EResponse extends Enum<EResponse>> {
    private long                        serialNumber;

    public MessageFactory() {
        serialNumber = Long.MIN_VALUE;
    }

    public long generateSerialNumber() { return serialNumber ++; }
    public abstract <MessageObject> Message makeMessage(EPacketType en, MessageObject arg)     throws MessageFactoryException;
    public abstract <MessageObject> Message makeRequest(EPacketType en, MessageObject arg)     throws MessageFactoryException;
    public abstract <MessageObject> Message makeResponse(EPacketType en, EResponse er, long serialNumber, MessageObject arg)    throws MessageFactoryException;
}
