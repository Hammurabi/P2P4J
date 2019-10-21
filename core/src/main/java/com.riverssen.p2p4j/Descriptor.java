package com.riverssen.p2p4j;

public class Descriptor <T> {
    private T struct;

    public Descriptor(T struct) {
        this.struct = struct;
    }

    public T getStruct() {
        return struct;
    }
}
