package com.riverssen;

public class Descriptor <T> {
    private T struct;

    public Descriptor(T struct) {
        this.struct = struct;
    }

    public T getStruct() {
        return struct;
    }
}
