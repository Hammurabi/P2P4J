package com.riverssen.p2p4j;

import java.net.Socket;

public interface ConnectionRequestCallback {
    void onEvent(Server server, Socket socket);
}
