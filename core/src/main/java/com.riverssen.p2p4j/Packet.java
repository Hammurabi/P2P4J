package com.riverssen.p2p4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class Packet {
    public abstract int getSize();
    public abstract long getSerialNumber();
    @Deprecated
    public abstract byte[] getMessageData();
    public abstract ByteBuffer getBuffer();
    /**
     * @param buffer the buffer which to fill with data.
     *               this buffer must not be flipped.
     */
    public abstract void fill(ByteBuffer buffer);

    public abstract void write(DataOutputStream dataOutputStream) throws IOException;

    public abstract byte[] getHashCode();
    public abstract byte[] getBytes();
}
