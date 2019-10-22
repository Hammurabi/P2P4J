package com.riverssen.p2p4j;

public class DefaultScheme extends CryptographicScheme {
    @Override
    public byte[] encrypt(Packet packet) {
        return packet.getBytes();
    }

    @Override
    public byte[] decrypt(byte[] bytes) {
        return bytes;
    }
}
