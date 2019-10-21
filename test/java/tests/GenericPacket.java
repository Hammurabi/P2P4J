package tests;

import main.java.com.riverssen.p2p4j.Packet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class GenericPacket extends Packet {
    private byte data[];

    public GenericPacket(byte[] packetData) {
        this.data = packetData;
    }

    @Override
    public int getSize() {
        return data.length;
    }

    @Override
    public long getSerialNumber() {
        return getBuffer().getLong();
    }

    @Override
    public byte[] getMessageData() {
        return new byte[0];
    }

    @Override
    public ByteBuffer getBuffer() {
        return ByteBuffer.wrap(data);
    }

    @Override
    public void fill(ByteBuffer buffer) {
        buffer.put(data);
    }

    @Override
    public void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(data);
    }
}
