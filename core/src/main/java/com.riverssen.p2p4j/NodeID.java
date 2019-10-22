package com.riverssen.p2p4j;

import java.net.InetAddress;

public abstract class NodeID {
    public abstract boolean getShouldBan();
    public abstract void setShouldBan(boolean ban);

    public abstract int getPort();
    public abstract InetAddress getAddress();

    public abstract Descriptor getDescriptorStruct();

    public abstract int getSpam();
    public abstract void incrementSpam();

    public abstract boolean screen(byte[] object);
    public abstract void cache(byte[] object);

    public abstract CryptographicScheme getScheme();

    @Override
    public String toString() {
        return getAddress() + ":" + getPort();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}