package com.riverssen;

import java.util.concurrent.CompletableFuture;

public class MessageFulfillRequest {
    private CompletableFuture<Packet>   future;
    private long                        time;
    private long                        timeOut;

    public MessageFulfillRequest(long time, long timeOut)
    {
        this.future = new CompletableFuture<>();
        this.time   = time;
        this.timeOut= timeOut;
    }

    public boolean shouldRemove()
    {
        return (System.currentTimeMillis() - time) > (timeOut);
    }

    public CompletableFuture<Packet> getFuture() {
        return future;
    }
}
