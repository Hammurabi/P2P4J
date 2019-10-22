package com.riverssen.p2p4j;

public abstract class CryptographicScheme {
    public abstract byte[] encrypt(Packet packet);
    public abstract byte[] decrypt(byte[] bytes);
}
